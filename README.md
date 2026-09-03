# CareSync HMS — Patient Backend

A Spring Boot backend for the **Patient Module** of the CareSync Hospital Management System (HMS).

This repository contains the Patient-module portion of the CareSync HMS backend, including patient management, authentication, JWT-based security, role-based access control, MySQL database scripts, and demo data.

---

## 📌 Project Overview

**CareSync HMS** is a Hospital Management System designed to manage patients, doctors, departments, authentication, and related hospital operations.

This repository contains the **Patient Module** and the backend components required to run it independently.

### Main technologies

* **Java**
* **Spring Boot 3.4.5**
* **Spring Security**
* **JWT Authentication**
* **Maven**
* **MySQL / MariaDB**
* **XAMPP**
* **REST API**

---

# 📁 Project Structure

```text
CareSync-Patient-Backend/
│
├── database/
│   ├── 00_create_database.sql
│   ├── 01_schema_patient.sql
│   └── 02_seed_data_patient.sql
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── caresync/
│       │           └── hms/
│       │               ├── HmsApplication.java
│       │               │
│       │               ├── config/
│       │               │   ├── CorsConfig.java
│       │               │   └── SecurityConfig.java
│       │               │
│       │               ├── controller/
│       │               │   ├── AuthController.java
│       │               │   └── PatientController.java
│       │               │
│       │               ├── dto/
│       │               │   ├── LoginRequest.java
│       │               │   ├── LoginResponse.java
│       │               │   ├── RegisterRequest.java
│       │               │   └── PatientDTO.java
│       │               │
│       │               ├── exception/
│       │               │   ├── GlobalExceptionHandler.java
│       │               │   ├── InvalidCredentialsException.java
│       │               │   └── ResourceNotFoundException.java
│       │               │
│       │               ├── model/
│       │               │   ├── User.java
│       │               │   ├── Patient.java
│       │               │   ├── Doctor.java
│       │               │   └── Department.java
│       │               │
│       │               ├── repository/
│       │               │   ├── UserRepository.java
│       │               │   ├── PatientRepository.java
│       │               │   ├── DoctorRepository.java
│       │               │   └── DepartmentRepository.java
│       │               │
│       │               ├── security/
│       │               │   ├── JwtAuthenticationFilter.java
│       │               │   └── JwtUtil.java
│       │               │
│       │               └── service/
│       │                   ├── AuthService.java
│       │                   └── PatientService.java
│       │
│       └── resources/
│           └── application.properties
│
├── pom.xml
├── README.md
└── .gitignore
```

---

# ⚙️ Requirements

Before running the project, install the following:

| Software                                                      | Purpose                   |
| ------------------------------------------------------------- | ------------------------- |
| **JDK 17 or compatible Java version required by the project** | Run Spring Boot           |
| **Maven**                                                     | Build and run the backend |
| **XAMPP**                                                     | MySQL/MariaDB database    |
| **Git**                                                       | Version control           |
| **Postman**                                                   | Test REST APIs            |
| **VS Code / IntelliJ IDEA**                                   | Development               |

Check Java:

```bash
java -version
```

Check Maven:

```bash
mvn -version
```

---

# 🗄️ Database Setup

The project uses **MySQL/MariaDB** and is designed to work with **XAMPP**.

## Step 1 — Start XAMPP

Open XAMPP Control Panel and start:

```text
Apache
MySQL
```

Apache is useful for phpMyAdmin, while MySQL/MariaDB runs the database server.

---

## Step 2 — Create the database

The database creation script is located at:

```text
database/00_create_database.sql
```

### Option A — Using phpMyAdmin

1. Open XAMPP.
2. Start **Apache** and **MySQL**.
3. Open:

```text
http://localhost/phpmyadmin
```

4. Open the **SQL** tab.
5. Copy and execute the contents of:

```text
database/00_create_database.sql
```

This creates:

```text
caresync_hms_db
```

---

## Step 3 — Create the tables

After creating the database, execute:

```text
database/01_schema_patient.sql
```

Make sure the database selected in phpMyAdmin is:

```text
caresync_hms_db
```

The schema contains the tables and foreign-key dependencies required by the Patient module.

---

## Step 4 — Insert demo data

Execute:

```text
database/02_seed_data_patient.sql
```

This inserts the demo users, departments, doctors, and patients required for testing.

---

# 🖥️ Database Setup Using Git Bash / MySQL CLI

If MySQL is available in your PATH, you can use:

```bash
mysql -u root -p < database/00_create_database.sql
```

Then:

```bash
mysql -u root -p caresync_hms_db < database/01_schema_patient.sql
```

Finally:

```bash
mysql -u root -p caresync_hms_db < database/02_seed_data_patient.sql
```

For a default XAMPP installation where the root user has no password, you may be prompted for a password depending on your configuration.

If `mysql` is not recognized in Git Bash, use the MySQL executable from your XAMPP installation, for example:

```bash
/c/xampp/mysql/bin/mysql.exe -u root -p < database/00_create_database.sql
```

---

# 🔐 Database Configuration

The application reads database credentials from environment variables.

The relevant settings are configured in:

```text
src/main/resources/application.properties
```

The expected environment variables are:

```text
DB_USERNAME
DB_PASSWORD
JWT_SECRET
CORS_ALLOWED_ORIGINS
```

For a standard XAMPP installation, the database configuration is commonly:

```text
DB_USERNAME=root
DB_PASSWORD=
```

Do **not** commit real passwords or secrets to GitHub.

---

# 🔑 JWT Configuration

The backend uses JWT for authentication.

Set the JWT secret using:

```text
JWT_SECRET
```

For example, in Git Bash:

```bash
export JWT_SECRET="your-long-random-secret"
```

On Windows PowerShell:

```powershell
$env:JWT_SECRET="your-long-random-secret"
```

For permanent Windows environment variables, configure them through:

```text
System Properties
→ Environment Variables
```

The project contains a placeholder/default value for development, but a real secret should be configured for actual deployment.

---

# 🌐 CORS Configuration

The default development CORS origin is:

```text
http://localhost:5173
```

This corresponds to the typical Vite development server.

You can override it using:

```text
CORS_ALLOWED_ORIGINS
```

Example:

```text
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

---

# ▶️ Running the Backend

Open Git Bash or the terminal in the project root:

```text
CareSync-Patient-Backend/
```

Then verify the project:

```bash
mvn clean
```

Build the project:

```bash
mvn clean package
```

If the build succeeds, start the Spring Boot application:

```bash
mvn spring-boot:run
```

Alternatively, run:

```text
HmsApplication.java
```

from your IDE.

---

# 🚀 Starting the Application

When Spring Boot starts successfully, you should see messages indicating that the application has started.

The backend will normally be available at:

```text
http://localhost:3000
```

The exact port is controlled by:

```text
application.properties
```

If your `application.properties` specifies a different port, use that port instead.

---

# 🔐 Authentication

The authentication API is:

```text
POST /api/auth/login
```

The login request uses JSON.

### Admin

```json
{
  "email": "admin@caresync.com",
  "password": "admin123"
}
```

### Doctor

```json
{
  "email": "doctor@demo.com",
  "password": "pass123"
}
```

### Patient

```json
{
  "email": "patient@demo.com",
  "password": "pass123"
}
```

A successful login returns a JWT token.

---

# 🧪 Testing With Postman

## 1. Login

Create a Postman request:

```text
POST http://localhost:3000/api/auth/login
```

Select:

```text
Body
→ raw
→ JSON
```

Use:

```json
{
  "email": "patient@demo.com",
  "password": "pass123"
}
```

Send the request.

Copy the JWT token returned by the backend.

---

## 2. Add the JWT token

For protected endpoints, add the following HTTP header:

```text
Authorization: Bearer YOUR_JWT_TOKEN
```

Example:

```text
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

You can also configure this through Postman's **Authorization → Bearer Token** option.

---

# 👤 Patient API Security

Patient endpoints are protected using:

* Spring Security
* JWT authentication
* Role-based access control
* Ownership validation for patient users

Patients cannot simply access every patient record.

A patient can access their own patient record when the authenticated account is linked to that record.

Administrative and doctor-level operations are restricted according to the configured security rules.

---

# 📝 Patient Registration

The public registration endpoint is:

```text
POST /api/auth/register
```

Self-registration does **not** allow users to create an `ADMIN` account.

Allowed self-registration roles are:

```text
PATIENT
DOCTOR
```

An administrator account must be created through an appropriate protected administrative process.

---

# 👨‍⚕️ Patient Account Linking

A patient can be associated with an existing user account using `userId`.

The Patient module also supports creating a login account when an email and password are supplied during patient creation.

If neither an existing `userId` nor account credentials are supplied, a patient record can exist without a login account.

This allows front-desk staff to create patient records without automatically creating login credentials.

---

# 🏥 Department and Doctor Validation

When both a department and assigned doctor are provided for a patient, the backend validates that:

```text
Doctor → belongs to → selected Department
```

A patient cannot be assigned to a doctor belonging to a different department.

---

# 🛡️ Security Features

The backend includes:

### JWT Authentication

Requests to protected APIs require a valid JWT.

### Role-Based Access Control

Access is controlled based on user roles such as:

```text
ADMIN
DOCTOR
PATIENT
```

### Account Status Validation

A deactivated user account cannot authenticate using an existing JWT once the request reaches the authentication filter.

### Patient Ownership

Patient users are restricted from accessing another patient's record.

### Input Validation

Patient data includes validation for fields such as:

* Gender
* Blood group
* Status
* Phone number

### Exception Handling

The project includes a global exception handler for consistent API error responses.

---

# 🗃️ Database Schema Management

The project uses manually maintained SQL scripts.

Hibernate is configured so that it does **not** automatically modify the database schema.

The schema is controlled by:

```text
database/01_schema_patient.sql
```

Therefore, database changes should be made through the SQL schema rather than relying on Hibernate to automatically modify tables.

The schema script can also be re-run safely with the included handling for the department/head-doctor constraint.

---

# 🔄 Re-running the Database Scripts

If you need to recreate the database from scratch:

1. Remove the existing database from MySQL/phpMyAdmin.
2. Run:

```text
00_create_database.sql
```

3. Run:

```text
01_schema_patient.sql
```

4. Run:

```text
02_seed_data_patient.sql
```

This provides a clean database containing the required Patient-module data.

---

# 🧹 Clean Build

If you encounter Maven compilation or stale build issues, run:

```bash
mvn clean
```

Then:

```bash
mvn clean package
```

Then:

```bash
mvn spring-boot:run
```

---

# 📦 Maven Dependencies

The project uses Maven for dependency management.

The main dependency configuration is located in:

```text
pom.xml
```

The backend uses Spring Boot components for:

* Web/REST APIs
* Data JPA
* MySQL/MariaDB connectivity
* Spring Security
* JWT authentication
* Validation

Unused WebSocket and mail dependencies were removed from this Patient-module package.

---

# 🔒 Git and GitHub

The project includes a `.gitignore` file to prevent build files, IDE configuration, logs, and environment secrets from being uploaded.

Important ignored files/folders include:

```text
target/
*.class
.idea/
.vscode/
*.iml
.env
.env.*
logs/
*.log
```

**Do not commit real database passwords, JWT secrets, API keys, or other credentials.**

---

# 🐛 Troubleshooting

## MySQL connection error

Check that:

* XAMPP MySQL is running.
* Database `caresync_hms_db` exists.
* `DB_USERNAME` is correct.
* `DB_PASSWORD` is correct.
* The MySQL port matches your configuration.

---

## `mysql` command not found

If Git Bash reports:

```text
mysql: command not found
```

use the XAMPP MySQL executable:

```bash
/c/xampp/mysql/bin/mysql.exe
```

---

## Maven command not found

If:

```bash
mvn
```

is not recognized, install Maven and configure its `bin` directory in the Windows PATH.

Then restart Git Bash and verify:

```bash
mvn -version
```

---

## Port already in use

If the configured application port is already being used by another application, stop the conflicting process or change the Spring Boot port in:

```text
application.properties
```

---

## Login returns 401

Check:

1. MySQL is running.
2. The seed data was imported.
3. The email and password are correct.
4. The user account is active.
5. The request body is valid JSON.
6. The JWT is being sent correctly for protected endpoints.

Example:

```http
Authorization: Bearer YOUR_TOKEN
```

---

## Patient endpoint returns 403

A `403 Forbidden` response generally means the request is authenticated but the authenticated user does not have permission to perform that operation.

Check the user's role and whether the patient record belongs to the authenticated account.

---

# 📋 Demo Credentials

| Role    | Email                | Password   |
| ------- | -------------------- | ---------- |
| Admin   | `admin@caresync.com` | `admin123` |
| Doctor  | `doctor@demo.com`    | `pass123`  |
| Patient | `patient@demo.com`   | `pass123`  |

> **Warning:** These are demonstration credentials only. Do not use them in a production environment.

---

# 📌 Scope of This Repository

This repository is a **partial Patient-module backend** extracted from the larger CareSync HMS codebase.

It includes the components necessary for the Patient module to operate, including:

* Patient management
* Authentication
* JWT security
* Role-based authorization
* User management dependencies
* Department dependencies
* Doctor dependencies
* MySQL database schema
* Demo data

The complete CareSync HMS system may contain additional modules that are not included in this repository.

---

# 🔮 Future Improvements

The following improvements can be added in future versions:

* Automated unit and integration tests
* Swagger/OpenAPI documentation
* Additional Patient APIs
* Full Department module
* Full Doctor module
* Appointment management
* Medical records
* Prescription management
* Frontend integration
* Production deployment configuration

---

# 📄 License

This project is developed for academic/project purposes as part of the CareSync HMS system.

---

# 👨‍💻 CareSync HMS

**Patient Module — Spring Boot Backend**

```text
Java + Spring Boot + Spring Security + JWT + MySQL/MariaDB
```
