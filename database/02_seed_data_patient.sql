-- CareSync HMS -- demo seed data (MySQL / MariaDB, e.g. XAMPP)
-- Passwords: admin123 (admin), pass123 (doctor/patient)
-- Usage: mysql -u root -p caresync_hms_db < 02_seed_data_patient.sql
--
-- PARTIAL UPDATE: seed rows for users, departments, doctors,
-- and patients only (kept because patients has FKs to them),
-- ported from the full project's seed file to MySQL syntax
-- (INSERT IGNORE instead of ON CONFLICT, MySQL date/interval syntax).

-- Users (BCrypt hashes -- compatible with Spring Security)
INSERT IGNORE INTO users (email, password_hash, role, is_active) VALUES
('admin@caresync.com', '$2a$10$TWbYxeixGabpjGSiA1EfDOk3cKwDIATCvVLR6/jeXmxclmuaFFyKW', 'ADMIN', true),
('doctor@demo.com', '$2a$10$tfu6kTwcfI8Mw8FOJlXIquejOCzCN6GBZlejvUwW3LjGqmBspseI.', 'DOCTOR', true),
('doctor2@demo.com', '$2a$10$tfu6kTwcfI8Mw8FOJlXIquejOCzCN6GBZlejvUwW3LjGqmBspseI.', 'DOCTOR', true),
('patient@demo.com', '$2a$10$tfu6kTwcfI8Mw8FOJlXIquejOCzCN6GBZlejvUwW3LjGqmBspseI.', 'PATIENT', true);

-- Departments
INSERT IGNORE INTO departments (department_name, department_code, floor_number, bed_capacity, occupied_beds, status) VALUES
('Cardiology', 'CARD', 2, 30, 18, 'Active'),
('Neurology', 'NEUR', 3, 25, 12, 'Active'),
('Emergency', 'EMRG', 1, 30, 29, 'Critical'),
('Gynecology', 'GYNE', 4, 20, 8, 'Active'),
('Orthopedics', 'ORTH', 5, 22, 10, 'Active');

-- Doctors
INSERT IGNORE INTO doctors (user_id, doctor_code, first_name, last_name, specialization, qualification, department_id, phone, years_of_experience, consultation_fee, available_days, is_available, joined_date)
SELECT u.user_id, 'DR-0001', 'Hasan', 'Mahmud', 'Cardiologist', 'MBBS, MD (Cardiology)', d.department_id, '01711-XXXXXX', 12, 800.00, 'Sun, Tue, Thu', true, '2020-01-15'
FROM users u, departments d
WHERE u.email = 'doctor@demo.com' AND d.department_code = 'CARD';

INSERT IGNORE INTO doctors (user_id, doctor_code, first_name, last_name, specialization, qualification, department_id, phone, years_of_experience, consultation_fee, available_days, is_available, joined_date)
SELECT u.user_id, 'DR-0002', 'Fatima', 'Akter', 'Neurologist', 'MBBS, FCPS (Neurology)', d.department_id, '01812-XXXXXX', 8, 1000.00, 'Mon, Wed, Sat', true, '2021-06-01'
FROM users u, departments d
WHERE u.email = 'doctor2@demo.com' AND d.department_code = 'NEUR';

-- Patients
INSERT IGNORE INTO patients (user_id, patient_code, first_name, last_name, date_of_birth, gender, blood_group, phone, emergency_contact, address, status)
SELECT u.user_id, 'PT-0001', 'Mohammed', 'Rahman', '1980-05-15', 'Male', 'A+', '01712-345678', '01812-456789', 'Dhaka, Bangladesh', 'Active'
FROM users u WHERE u.email = 'patient@demo.com';

INSERT IGNORE INTO patients (patient_code, first_name, last_name, date_of_birth, gender, blood_group, phone, address, status, admission_date) VALUES
('PT-0002', 'Ayesha', 'Begum', '1992-08-22', 'Female', 'B+', '01911-234567', 'Chittagong, Bangladesh', 'Admitted', NOW() - INTERVAL 3 DAY),
('PT-0003', 'Karim', 'Uddin', '1975-03-10', 'Male', 'O-', '01611-876543', 'Sylhet, Bangladesh', 'Critical', NOW() - INTERVAL 1 DAY);
