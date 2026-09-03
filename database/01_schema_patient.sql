-- ============================================================
-- CareSync HMS — MySQL Schema (3NF Normalized)
-- Database: caresync_hms_db
-- Compatible with XAMPP MariaDB 10.x / MySQL 8.x
--
-- PARTIAL UPDATE: Patient module only.
-- Includes the "patients" table plus the tables it has a
-- foreign key dependency on (users, departments, doctors),
-- extracted as-is from the full project schema.
-- Run 00_create_database.sql first - this file assumes the database
-- already exists and only selects it.
-- ============================================================

USE caresync_hms_db;

-- ============================================================
-- 1. USERS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login DATETIME NULL,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB;

-- ============================================================
-- 2. DEPARTMENTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS departments (
    department_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(50) UNIQUE NOT NULL,
    department_code VARCHAR(10) UNIQUE NOT NULL,
    head_doctor_id BIGINT NULL,
    floor_number INT NULL,
    building VARCHAR(50) NULL,
    bed_capacity INT DEFAULT 0,
    occupied_beds INT DEFAULT 0,
    nurse_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'Active',
    phone_extension VARCHAR(10) NULL,
    description TEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_departments_name (department_name)
) ENGINE=InnoDB;

-- ============================================================
-- 3. DOCTORS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS doctors (
    doctor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    doctor_code VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    qualification VARCHAR(200) NULL,
    department_id BIGINT NULL,
    phone VARCHAR(20) NOT NULL,
    years_of_experience INT NULL,
    consultation_fee DECIMAL(10, 2) DEFAULT 0,
    available_days VARCHAR(100) NULL,
    is_available TINYINT(1) DEFAULT 1,
    joined_date DATE NULL,
    avatar_data LONGTEXT NULL,
    signature_data LONGTEXT NULL,
    biography TEXT NULL,
    available_slots TEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE SET NULL,
    INDEX idx_doctors_code (doctor_code),
    INDEX idx_doctors_department (department_id),
    INDEX idx_doctors_specialization (specialization)
) ENGINE=InnoDB;

-- Add FK for head_doctor_id
-- MySQL/MariaDB have no "ADD CONSTRAINT IF NOT EXISTS", and CREATE TABLE IF
-- NOT EXISTS above doesn't protect this statement -- re-running this file
-- against a database that already has the table (but not yet this
-- constraint, or vice versa) throws "Duplicate key name" and aborts the
-- rest of the script. Guard it explicitly via information_schema so the
-- whole file can be re-run safely at any point.
DROP PROCEDURE IF EXISTS caresync_add_fk_head_doctor;

DELIMITER $$
CREATE PROCEDURE caresync_add_fk_head_doctor()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND TABLE_NAME = 'departments'
          AND CONSTRAINT_NAME = 'fk_head_doctor'
    ) THEN
        ALTER TABLE departments
            ADD CONSTRAINT fk_head_doctor
            FOREIGN KEY (head_doctor_id) REFERENCES doctors(doctor_id)
            ON DELETE SET NULL;
    END IF;
END$$
DELIMITER ;

CALL caresync_add_fk_head_doctor();
DROP PROCEDURE caresync_add_fk_head_doctor;

-- ============================================================
-- 4. PATIENTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS patients (
    patient_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    patient_code VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    blood_group VARCHAR(5) NULL,
    phone VARCHAR(20) NOT NULL,
    emergency_contact VARCHAR(20) NULL,
    address TEXT NULL,
    department_id BIGINT NULL,
    assigned_doctor_id BIGINT NULL,
    initial_diagnosis TEXT NULL,
    status VARCHAR(20) DEFAULT 'Active',
    admission_date DATETIME NULL,
    discharge_date DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_doctor_id) REFERENCES doctors(doctor_id) ON DELETE SET NULL,
    INDEX idx_patients_code (patient_code),
    INDEX idx_patients_status (status),
    INDEX idx_patients_name (first_name, last_name),
    INDEX idx_patients_department (department_id)
) ENGINE=InnoDB;

-- ============================================================
