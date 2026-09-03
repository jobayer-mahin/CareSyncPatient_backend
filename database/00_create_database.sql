-- CareSync HMS -- create database (MySQL / MariaDB, e.g. XAMPP)
-- Usage: mysql -u root -p < 00_create_database.sql
-- (or run this from phpMyAdmin's SQL tab)

CREATE DATABASE IF NOT EXISTS caresync_hms_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
