package com.caresync.hms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {

    private Long id;
    private Long userId;
    private String email;

    // Only used on POST /api/patients, to opt in to creating a login account
    // for this patient in the same request (see PatientService for the
    // create-vs-link decision). Never populated on responses -- convertToDTO
    // never sets it, so it can't leak back to the caller.
    private String password;

    private String patientCode;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    private String gender;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Blood group must be one of A+, A-, B+, B-, AB+, AB-, O+, O-")
    private String bloodGroup;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Phone must be 7-20 characters using digits, spaces, +, -, ( or )")
    private String phone;

    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Emergency contact must be 7-20 characters using digits, spaces, +, -, ( or )")
    private String emergencyContact;
    private String address;

    // Department info
    private Long departmentId;
    private String departmentName;

    // Assigned doctor info
    private Long assignedDoctorId;
    private String assignedDoctorName;

    private String initialDiagnosis;

    @Pattern(regexp = "^(Active|Admitted|Discharged|Critical)$", message = "Status must be Active, Admitted, Discharged, or Critical")
    private String status;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
