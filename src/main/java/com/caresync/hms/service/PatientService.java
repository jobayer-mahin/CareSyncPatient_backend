package com.caresync.hms.service;

import com.caresync.hms.dto.PatientDTO;
import com.caresync.hms.exception.ResourceNotFoundException;
import com.caresync.hms.model.Department;
import com.caresync.hms.model.Doctor;
import com.caresync.hms.model.Patient;
import com.caresync.hms.model.User;
import com.caresync.hms.repository.DepartmentRepository;
import com.caresync.hms.repository.DoctorRepository;
import com.caresync.hms.repository.PatientRepository;
import com.caresync.hms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private static final int MAX_CODE_GENERATION_ATTEMPTS = 5;

    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id, Authentication authentication) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));

        assertViewable(patient, authentication);
        return convertToDTO(patient);
    }

    // ADMIN and DOCTOR can view any patient.
    // A PATIENT can only view the patient record linked to their own account.
    private void assertViewable(Patient patient, Authentication authentication) {

        boolean isPatientRole = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));

        if (!isPatientRole) {
            return;
        }

        String requesterEmail = authentication.getName();

        boolean isOwnRecord = patient.getUser() != null
                && patient.getUser().getEmail() != null
                && patient.getUser().getEmail().equalsIgnoreCase(requesterEmail);

        if (!isOwnRecord) {
            throw new AccessDeniedException(
                    "You can only access your own patient record"
            );
        }
    }

    @Transactional
    public PatientDTO createPatient(PatientDTO patientDTO) {

        if (patientDTO.getStatus() == null || patientDTO.getStatus().isBlank()) {
            patientDTO.setStatus("Active");
        }

        // Resolve/validate the account, department and doctor before
        // entering the patient-code retry loop.
        User user = resolveOrCreateAccount(patientDTO);

        Department dept = null;

        if (patientDTO.getDepartmentId() != null) {
            dept = departmentRepository.findById(patientDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department",
                            "id",
                            patientDTO.getDepartmentId()
                    ));
        }

        Doctor doc = null;

        if (patientDTO.getAssignedDoctorId() != null) {
            doc = doctorRepository.findById(patientDTO.getAssignedDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Doctor",
                            "id",
                            patientDTO.getAssignedDoctorId()
                    ));
        }

        validateDoctorBelongsToDepartment(doc, dept);

        // Generate a patient code and retry if a database unique constraint
        // is hit because another request generated the same code.
        DataIntegrityViolationException lastFailure = null;

        for (int attempt = 0;
             attempt < MAX_CODE_GENERATION_ATTEMPTS;
             attempt++) {

            Patient patient = buildPatientEntity(
                    patientDTO,
                    user,
                    dept,
                    doc
            );

            Long maxId = patientRepository.findMaxId();

            patient.setPatientCode(
                    "PT-" + String.format(
                            "%04d",
                            maxId + 1 + attempt
                    )
            );

            try {
                Patient savedPatient = patientRepository.saveAndFlush(patient);
                return convertToDTO(savedPatient);

            } catch (DataIntegrityViolationException ex) {
                lastFailure = ex;
            }
        }

        throw new IllegalStateException(
                "Could not generate a unique patient code after "
                        + MAX_CODE_GENERATION_ATTEMPTS
                        + " attempts",
                lastFailure
        );
    }

    @Transactional
    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient",
                        "id",
                        id
                ));

        existingPatient.setFirstName(patientDTO.getFirstName());
        existingPatient.setLastName(patientDTO.getLastName());
        existingPatient.setDateOfBirth(patientDTO.getDateOfBirth());
        existingPatient.setGender(patientDTO.getGender());
        existingPatient.setBloodGroup(patientDTO.getBloodGroup());
        existingPatient.setPhone(patientDTO.getPhone());
        existingPatient.setEmergencyContact(patientDTO.getEmergencyContact());
        existingPatient.setAddress(patientDTO.getAddress());
        existingPatient.setInitialDiagnosis(patientDTO.getInitialDiagnosis());
        existingPatient.setStatus(patientDTO.getStatus());
        existingPatient.setAdmissionDate(patientDTO.getAdmissionDate());
        existingPatient.setDischargeDate(patientDTO.getDischargeDate());

        // PUT is treated as a full replacement.
        // If departmentId or assignedDoctorId is null,
        // the existing relationship will be cleared.
        Department dept = null;

        if (patientDTO.getDepartmentId() != null) {
            dept = departmentRepository.findById(
                            patientDTO.getDepartmentId()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department",
                            "id",
                            patientDTO.getDepartmentId()
                    ));
        }

        Doctor doc = null;

        if (patientDTO.getAssignedDoctorId() != null) {
            doc = doctorRepository.findById(
                            patientDTO.getAssignedDoctorId()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Doctor",
                            "id",
                            patientDTO.getAssignedDoctorId()
                    ));
        }

        validateDoctorBelongsToDepartment(doc, dept);

        existingPatient.setDepartment(dept);
        existingPatient.setAssignedDoctor(doc);

        Patient updatedPatient = patientRepository.save(existingPatient);

        return convertToDTO(updatedPatient);
    }

    @Transactional
    public void deletePatient(Long id) {

        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Patient",
                    "id",
                    id
            );
        }

        patientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PatientDTO> searchPatients(String searchTerm) {

        return patientRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        searchTerm,
                        searchTerm
                )
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientDTO> getPatientsByStatus(String status) {

        return patientRepository
                .findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // DTO conversion
    // -------------------------------------------------------------------------

    private PatientDTO convertToDTO(Patient patient) {

        PatientDTO dto = new PatientDTO();

        dto.setId(patient.getId());
        dto.setPatientCode(patient.getPatientCode());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setBloodGroup(patient.getBloodGroup());
        dto.setPhone(patient.getPhone());
        dto.setEmergencyContact(patient.getEmergencyContact());
        dto.setAddress(patient.getAddress());
        dto.setInitialDiagnosis(patient.getInitialDiagnosis());
        dto.setStatus(patient.getStatus());
        dto.setAdmissionDate(patient.getAdmissionDate());
        dto.setDischargeDate(patient.getDischargeDate());
        dto.setCreatedAt(patient.getCreatedAt());
        dto.setUpdatedAt(patient.getUpdatedAt());

        if (patient.getUser() != null) {
            dto.setUserId(patient.getUser().getId());
            dto.setEmail(patient.getUser().getEmail());
        }

        if (patient.getDepartment() != null) {
            dto.setDepartmentId(patient.getDepartment().getId());
            dto.setDepartmentName(
                    patient.getDepartment().getDepartmentName()
            );
        }

        if (patient.getAssignedDoctor() != null) {
            dto.setAssignedDoctorId(
                    patient.getAssignedDoctor().getId()
            );

            dto.setAssignedDoctorName(
                    "Dr. "
                            + patient.getAssignedDoctor().getFirstName()
                            + " "
                            + patient.getAssignedDoctor().getLastName()
            );
        }

        return dto;
    }

    // -------------------------------------------------------------------------
    // Patient entity construction
    // -------------------------------------------------------------------------

    private Patient buildPatientEntity(
            PatientDTO dto,
            User user,
            Department department,
            Doctor doctor) {

        Patient patient = new Patient();

        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setPhone(dto.getPhone());
        patient.setEmergencyContact(dto.getEmergencyContact());
        patient.setAddress(dto.getAddress());
        patient.setInitialDiagnosis(dto.getInitialDiagnosis());
        patient.setStatus(dto.getStatus());
        patient.setAdmissionDate(dto.getAdmissionDate());
        patient.setDischargeDate(dto.getDischargeDate());

        patient.setUser(user);
        patient.setDepartment(department);
        patient.setAssignedDoctor(doctor);

        return patient;
    }

    // -------------------------------------------------------------------------
    // User account handling
    // -------------------------------------------------------------------------

    private User resolveOrCreateAccount(PatientDTO dto) {

        /*
         * Case 1:
         * A userId was supplied.
         *
         * We use an existing account, but we MUST verify that
         * the account belongs to a PATIENT.
         */
        if (dto.getUserId() != null) {

            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User",
                            "id",
                            dto.getUserId()
                    ));

            // IMPORTANT:
            // Prevent ADMIN or DOCTOR accounts from being linked
            // to a Patient record.
            if (!"PATIENT".equalsIgnoreCase(user.getRole())) {
                throw new IllegalArgumentException(
                        "The linked user account must have the PATIENT role"
                );
            }

            return user;
        }

        /*
         * Case 2:
         * No userId was supplied and no email was supplied.
         *
         * Create a patient record without a login account.
         */
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            return null;
        }

        /*
         * Case 3:
         * Email was supplied, so a new PATIENT login account
         * can be created.
         */
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already registered: " + dto.getEmail()
            );
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException(
                    "A password is required to create a login account "
                            + "for this patient "
                            + "(omit email entirely to create the patient "
                            + "with no login)"
            );
        }

        User newUser = new User();

        newUser.setEmail(dto.getEmail());

        // Password is stored as a BCrypt hash, never plain text.
        newUser.setPasswordHash(
                passwordEncoder.encode(dto.getPassword())
        );

        newUser.setRole("PATIENT");
        newUser.setIsActive(true);

        return userRepository.save(newUser);
    }

    // -------------------------------------------------------------------------
    // Doctor / Department validation
    // -------------------------------------------------------------------------

    private void validateDoctorBelongsToDepartment(
            Doctor doctor,
            Department department) {

        // Nothing to validate if one of the associations is not supplied.
        if (doctor == null
                || department == null
                || doctor.getDepartment() == null) {
            return;
        }

        if (!doctor.getDepartment()
                .getId()
                .equals(department.getId())) {

            throw new IllegalArgumentException(
                    "Doctor '"
                            + doctor.getFirstName()
                            + " "
                            + doctor.getLastName()
                            + "' belongs to department '"
                            + doctor.getDepartment().getDepartmentName()
                            + "', not the requested department '"
                            + department.getDepartmentName()
                            + "'"
            );
        }
    }
}
