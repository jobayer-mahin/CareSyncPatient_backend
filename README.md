# CareSync HMS — Patient Module (Partial Update, revised again)

This is a revision of the earlier Patient-module submission after review
feedback. Everything below is still just the Patient slice of the full
CareSync HMS — extracted from the same codebase, not rewritten from
scratch — plus the fixes described in "What changed."

## Setup (MySQL via XAMPP)

1. Start Apache + MySQL in XAMPP.
2. Create the database:
   ```
   mysql -u root -p < database/00_create_database.sql
   ```
   (or paste its contents into phpMyAdmin's SQL tab)
3. Load the schema, then the demo data:
   ```
   mysql -u root -p caresync_hms_db < database/01_schema_patient.sql
   mysql -u root -p caresync_hms_db < database/02_seed_data_patient.sql
   ```
4. Set `DB_USERNAME` / `DB_PASSWORD` as environment variables if your XAMPP
   MySQL isn't the default `root` with no password (see `application.properties`
   for all overridable settings — DB credentials, JWT secret, CORS origin).
5. Run `mvn spring-boot:run` (or run `HmsApplication` from your IDE).
6. Demo login: `POST /api/auth/login` with
   `{"email":"admin@caresync.com","password":"admin123"}` (or
   `doctor@demo.com` / `patient@demo.com`, password `pass123`) to get a JWT,
   then send it as `Authorization: Bearer <token>` on `/api/patients/**`.

## What's included

**Database** (`database/`) — MySQL/MariaDB syntax throughout:
- `00_create_database.sql` — creates the database
- `01_schema_patient.sql` — `patients` table + its FK dependencies (`users`, `departments`, `doctors`)
- `02_seed_data_patient.sql` — demo rows for those same four tables

**Backend** (`src/main/java/com/caresync/hms/`):
- `model/` — `Patient`, `User`, `Department`, `Doctor`
- `dto/` — `PatientDTO`, `LoginRequest`, `LoginResponse`, `RegisterRequest`
- `repository/` — `PatientRepository`, `UserRepository`, `DepartmentRepository`, `DoctorRepository`
- `service/` — `PatientService`, `AuthService`
- `controller/` — `PatientController`, `AuthController`
- `security/` — `JwtUtil`, `JwtAuthenticationFilter`
- `config/` — `SecurityConfig` (role-based access rules), `CorsConfig`
- `exception/` — `ResourceNotFoundException`, `InvalidCredentialsException`, `GlobalExceptionHandler`
- `HmsApplication.java`, `pom.xml`, `application.properties`

`User`, `Department`, `Doctor` are included only because `Patient` depends
on them directly (a `User` link, and FKs to the other two). The auth/JWT
pieces are included so the patient endpoints in this ZIP are actually
protected rather than just described as protected — this is the same
`SecurityConfig`/`JwtUtil` used by the full project, not new code.

## What changed since the last submission

| # | Issue | Fix |
|---|---|---|
| 1 | `00_create_database.sql` used PostgreSQL syntax | Rewritten as plain MySQL `CREATE DATABASE IF NOT EXISTS` |
| 2 | `02_seed_data_patient.sql` used `ON CONFLICT`, `::date`, Postgres intervals | Rewritten with `INSERT IGNORE`, MySQL date literals, `INTERVAL n DAY` |
| 3 | `User.java` was missing | Added (it was simply left out of the first extraction) |
| 4 | `patient.user_id` was never set when creating a patient | `PatientService.convertToEntity` now loads and links the `User` when `userId` is supplied |
| 5–8 | No auth/JWT/RBAC in the package, so patient APIs weren't actually protected | Added `SecurityConfig`, `JwtUtil`, `JwtAuthenticationFilter`, `AuthController`, `AuthService` (all pulled from the full project, not newly written) — `/api/patients/**` now requires a valid JWT with an appropriate role |
| 9 | Patient code generation (`MAX(id)+1`) could produce duplicates under concurrent requests | `createPatient` now retries on a unique-constraint violation instead of trusting a single read |
| 10 | Clearing `departmentId`/`assignedDoctorId` on update didn't clear the assignment | `updatePatient` now explicitly nulls the relation when the field is omitted, matching normal PUT/replace semantics |
| 11–14 | Gender / blood group / status / phone accepted any non-blank string | Added `@Pattern` validation to `PatientDTO` for each |
| 19 | Hard-coded JWT secret | `jwt.secret` now reads from `JWT_SECRET` env var (placeholder default is clearly marked "not a real secret") |
| 20 | Hard-coded DB credentials | `spring.datasource.username`/`password` now read from `DB_USERNAME`/`DB_PASSWORD` env vars |
| 21 | Hard-coded mail credentials | Removed — `spring-boot-starter-mail` isn't used anywhere in this module (no `EmailService` here), so the config was dead weight, not a real feature |
| 24/25 | Unused WebSocket/Mail dependencies in `pom.xml` | Removed both (see note in `pom.xml`) |
| 17 | Database creation duplicated in SQL + `application.properties` | Removed `createDatabaseIfNotExist` from the JDBC URL; `00_create_database.sql` is now the single source of truth |
| 18 | `ddl-auto=update` on a hand-written schema | Set to `ddl-auto=none`; the SQL scripts own the schema |
| 22 | CORS origins broader than needed | Default narrowed to the Vite dev origin (`http://localhost:5173`), still overridable via `CORS_ALLOWED_ORIGINS` |
| 23 | Verbose SQL/debug logging on by default | `show-sql` off, log levels default to `INFO`, both overridable via env vars |
| 32 | README referenced the old Postgres commands | Rewritten above for MySQL/XAMPP |

## What changed in this round

| # | Issue | Fix |
|---|---|---|
| A | `POST /api/auth/register` (public, `permitAll`) could mint an `ADMIN` account | `AuthService.register` now rejects `role=ADMIN` outright; only `PATIENT`/`DOCTOR` can self-register. Real admin accounts need to be created by an already-authenticated admin through a separate protected endpoint, which is out of scope for this Patient-module ZIP |
| B | A `PATIENT` JWT could `GET` any patient by id, list all patients, or search/filter — the only role check was "are you *a* patient", not "is this record yours" | `SecurityConfig` now only lets `PATIENT` reach `GET /api/patients/{id}`; list/search/status stay `ADMIN`/`DOCTOR`-only. `PatientService.getPatientById` additionally checks that the patient row's linked `User.email` matches the caller, and throws `AccessDeniedException` (mapped to 403 in `GlobalExceptionHandler`) otherwise |
| C | `01_schema_patient.sql`'s `ALTER TABLE departments ADD CONSTRAINT fk_head_doctor ...` had no guard, so re-running the script against an already-provisioned database failed with "Duplicate key name" | Wrapped it in a small stored procedure that checks `information_schema.TABLE_CONSTRAINTS` first, calls itself, then drops itself — the whole file can now be re-run at any point |
| D | Nothing stopped a patient being saved with a department and an assigned doctor who actually belongs to a *different* department | `PatientService.validateDoctorBelongsToDepartment` runs on both create and update whenever both are supplied, and rejects the mismatch with a clear message |
| E | `JwtAuthenticationFilter` never checked `isActive`, so a JWT issued before an account was deactivated kept authenticating until it expired | The filter now requires `user.getIsActive() == true` in addition to signature/expiry validation before it builds an `Authentication` |
| F | Undecided: should creating a patient also create their login? | Decision, implemented in `PatientService.resolveOrCreateAccount` (see the comment there): **no, not by default.** `userId` links an existing account; `email` + `password` (new `PatientDTO.password`, opt-in, never echoed back) creates one in the same transaction; neither means a login-less patient record, which stays the common case for front-desk-entered patients |

Fixing B, D and F meant restructuring `PatientService.createPatient`: account/department/doctor resolution now happens **once**, before the patient-code retry loop, instead of inside it (`convertToEntity` → `buildPatientEntity`). Doing it inside the loop, as the previous revision did, meant a patient-code collision on attempt 1 would try to re-create the same new login account on attempt 2 and fail with a misleading "email already registered" error.

## Points I did not change, and why

- **#15/#16 (Department ↔ Doctor circular FK / `headDoctorId` as a raw `Long` instead of a mapped relation)** — real, but it's a Department-module design decision, not something the Patient module touches. Changing it here would mean redesigning `Department`/`Doctor` beyond the scope of a Patient-only update; better addressed when that module is submitted.
- **#26/#27/#28 (some JWT deps only used by auth-adjacent code, `InvalidCredentialsException`, a couple of repository query methods)** — now that auth is included, the JWT dependencies and `InvalidCredentialsException` are in active use. `PatientRepository.findRecentByStatus()`/`countByStatus()` remain unused by the current controller — harmless, but flagged here rather than deleted, since removing repository methods that aren't yet wired up is a judgment call for you to make, not something I should quietly do to someone else's repository interface.
- **#29/#30 (no automated tests, no Swagger/OpenAPI)** — real gaps, but adding a test suite or API docs is new work, not a fix to existing code, so it's out of scope for "fix what's broken." Worth doing as a follow-up if your faculty expects it.
- **#31/#33/#34 (no frontend in this ZIP; it's a partial module; it should eventually merge into the main backend)** — these describe the nature of a partial update, not defects in it. That's the intended scope of this deliverable.
