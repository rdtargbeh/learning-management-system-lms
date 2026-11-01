# LMS backend

Spring Boot 3.4 / Java 21 backend for the multi-tenant Learning Management System, aligned with **schema v3.1**
(PostgreSQL, 264 tables in 30 domains).

## What's in here

The code is organised by layer. Every layer is one flat package under `learning_management_system.backend`, so a
domain's classes are found by name (`Course`, `CourseDto`, `CourseMapper`, `CourseRepository`, `CourseService`,
`CourseServiceImplementation`, `CourseController`).

| Package | Contents |
|---|---|
| `entity/` | One JPA entity per table (264), generated from the live schema. |
| `dto/` | Request and response records, one per file (`UserDetailDto`, `CreateUserRequest`, `PageResponse` ...). |
| `mapper/` | MapStruct mappers between entities and DTOs, including PATCH updates (null = leave unchanged). |
| `controller/` | REST controllers. They only call services. |
| `repository/` | A Spring Data repository per entity. Tenancy and identity repositories are hand-written, with their queries. |
| `service/` | Service interfaces: what each feature offers. |
| `serviceimpl/` | The implementations (`...ServiceImplementation`): permission checks, transactions, business rules. |
| `enums/` | Allowed values from the schema's CHECK constraints (272, generated). A name that more than one table uses gets the entity name in front (`CourseSectionStatus`). |
| `security/` | JWT tokens, request context, permission checks, tenant stamping of new rows, trusted system transactions. |
| `config/` | Spring configuration: security, row-level-security transaction manager, MapStruct settings, first-start bootstrap. |
| `exception/` | `ApiException` and the global error handler. |
| `utility/` | Small helpers (`Patch`). |
| `specification/` | For JPA Specifications (dynamic search filters) when a feature needs them. |
| `src/main/resources/db/schema/` | The database schema: one SQL file per domain, plus `run_all.sql`. This is the source of truth. |
| `scripts/` | `gen_entities.py` (regenerates entities, repositories and enums), `catalog.sql`, `api_smoke_test.py` (61 end-to-end checks). |

So far services and controllers exist for **tenancy and identity**.

## How multi-tenancy works
- One shared schema, `lms_core`. Every table has `tenant_id`, and PostgreSQL row-level security (RLS) enforces isolation.
- Every request carries a JWT that holds the user, the tenant (absent for platform staff), the user type, and permissions.
- `TenantAwareTransactionManager` runs this at the start of **every** transaction:
  `set_config('app.current_tenant_id' | 'app.current_user_id' | 'app.is_platform_admin', …, true)`.
  The values are transaction-local, so pooled connections never carry one tenant into another request.
- The app must connect as a **non-superuser** role that is a member of `lms_app`. Superusers bypass RLS.
- Sign-in lookups and support-access bookkeeping run through `SystemTx`, a trusted server-side context. Nothing else uses it.

## Users (platform vs tenant)
- All accounts are created in `users`. Platform staff have `is_system_user = TRUE` and `tenant_id = NULL`. Institution users have a tenant and a `user_type`.
- A database trigger creates `user_profile` plus the type's profile: `student_profile`, `employee_profile` (faculty, TAs, staff, admins), `academic_advisor_profile` (advisors), or `platform_user_profile`.
- Observers have no profile of their own; `observer_student_link` records who may follow which student.
- Users are never hard-deleted (academic records block it): `POST /users/{id}/deactivate`.
- Platform staff enter an institution only through an approved, time-boxed **support access** session, which is audited in `audit_log`.

## Run locally
1. **Database** (PostgreSQL 14+):
   ```
   createdb lms
   cd src/main/resources/db/schema && psql -d lms -v ON_ERROR_STOP=1 -f run_all.sql
   psql -d lms -c "CREATE ROLE lms_api LOGIN PASSWORD '<choose one>' IN ROLE lms_app;"
   ```
2. **Environment** (never commit real values):
   ```
   LMS_DB_URL=jdbc:postgresql://localhost:5432/lms
   LMS_DB_USER=lms_api
   LMS_DB_PASSWORD=<the password above>
   LMS_JWT_SECRET=<32+ random bytes, e.g. openssl rand -base64 48>
   LMS_BOOTSTRAP_ADMIN_EMAIL=you@yourcompany.com        # first start only: creates the platform super admin
   LMS_BOOTSTRAP_ADMIN_PASSWORD=<12+ characters>
   ```
3. `./mvnw spring-boot:run`. Hibernate runs with `ddl-auto: validate`, so it checks that every entity matches the schema and never changes the database.
4. Optional: `python scripts/api_smoke_test.py` against a fresh database.

## API (v1)
Every endpoint except `POST /api/v1/auth/login` requires `Authorization: Bearer <token>`.

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/login` (`tenantCode` omitted for platform staff) · `GET /auth/me` · `POST /auth/password` |
| Platform (staff) | `POST/GET /platform/tenants` · `GET/PATCH /platform/tenants/{id}` · `POST/GET /platform/users` · `PATCH /platform/users/{id}/platform-profile` |
| Support access | `POST/GET /platform/support-access` · `POST /platform/support-access/{id}/approve|deny|start|end` |
| Users | `POST/GET /users` (filters: `type`, `status`, `q`, paging) · `GET/PATCH /users/{id}` · `POST /users/{id}/deactivate|reactivate|suspend` · `PUT /users/{id}/password` |
| Profiles | `PATCH /users/{id}/profile` · `/student-profile` · `/employee-profile` · `/advisor-profile` |
| Roles | `GET/POST /roles` · `PATCH /roles/{id}` · `GET /permissions` · `GET/POST /users/{id}/roles` · `DELETE /users/{id}/roles/{userRoleId}` |
| Observers | `POST /observer-links` · `POST /observer-links/{id}/revoke` · `GET /users/{id}/observers` · `GET /users/{id}/observing` |
| Institution | `GET /tenant` · `GET/PATCH /tenant/settings` |

Authorization: institution admins (`TENANT_ADMIN`) can do everything in their institution. Other users need permissions
granted through roles (`USER_READ`, `USER_MANAGE`, `ROLE_MANAGE`, `OBSERVER_MANAGE`, `TENANT_SETTINGS_MANAGE`). Everyone can read and edit their own basic profile.

## After a schema change
```
psql -d lms -At -f scripts/catalog.sql > scripts/catalog.json
python scripts/gen_entities.py
```
Never edit generated entities or enums by hand. Put custom queries in the hand-written repositories listed in `gen_entities.py`
(the generator never overwrites those).

## Not done yet
- Services and controllers for the other 28 domains: courses and enrollment, content, assignments, grading, quizzes, etc. The entities and repositories for them already exist.
- MFA verification at sign-in. Platform users are flagged `mfa_required`, but TOTP is not enforced yet.
- Refresh tokens and sign-out (tokens are short-lived access tokens only).
- SSO (SAML/OIDC) sign-in flows.
