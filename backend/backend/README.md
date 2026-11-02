# LMS backend

Spring Boot 3.4 / Java 21 backend for the multi-tenant Learning Management System, aligned with **schema v3.1**
(PostgreSQL, 264 tables in 30 domains).

## What's in here

The code is organised by layer. Every layer is one flat package under `learning_management_system.backend`, so a
table's classes are found by name (`Course`, `CourseDto`, `CourseMapper`, `CourseRepository`, `CourseService`,
`CourseServiceImplementation`, `CourseController`). Each implemented table has its own service, implementation and
controller (domains 01 and 02 completely). The only services without a table of their own are helpers: `AuthService`
(the sign-in flow), `UserProvisioningService` (account creation) and `MailService`.

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
| `utility/` | Small helpers: `Patch`, `ClientInfo` (IP, user agent), `DnsTxtResolver` (domain verification), `Tokens` (random tokens, SHA-256), `Totp` (RFC 6238 authenticator codes), `SecretCipher` (AES-GCM for stored MFA secrets). |
| `specification/` | JPA Specifications for dynamic search filters (`AuditLogSpecification`). |
| `src/main/resources/db/schema/` | The database schema: one SQL file per domain, plus `run_all.sql`. This is the source of truth. |
| `scripts/` | `gen_entities.py` (regenerates entities, repositories and enums), `catalog.sql`, `api_smoke_test.py` (61 identity/tenancy checks), `api_test_courses.py` (184 course/enrollment checks), `api_test_content.py` (145 content/module checks), `api_test_assignments.py` (194 assignment checks), `api_test_identity.py` (268 checks: branding, plan limits, institution record, domains, sessions, MFA, password reset, locks, audit log, profiles, role permissions, settings, accommodations). Run them in that order on a fresh database; the last one reads mailed tokens from the app log (`LMS_APP_LOG`). |

So far services and controllers exist for **tenancy, identity, academic structure, courses, enrollment, content, modules and assignments**.
Every table of domains 01 (tenancy) and 02 (identity) is now covered.

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

On first start the app creates the `lms_core` schema from `src/main/resources/db/schema` when it is missing
(`lms.schema.auto-create`, default true; needs a role that may create schemas, such as `postgres`). An existing schema is
never changed. Keep `spring.jpa.hibernate.ddl-auto=validate`.

1. **Database** (PostgreSQL 14+):
   ```
   createdb lms-app
   cd src/main/resources/db/schema && psql -d lms-app -v ON_ERROR_STOP=1 -f run_all.sql
   psql -d lms-app -c "CREATE ROLE lms_api LOGIN PASSWORD '<choose one>' IN ROLE lms_app;"
   ```
2. **Environment** (never commit real values):
   ```
   LMS_DB_URL=jdbc:postgresql://localhost:5432/lms-app
   LMS_DB_USER=lms_api
   LMS_DB_PASSWORD=<the password above>
   LMS_JWT_SECRET=<32+ random bytes, e.g. openssl rand -base64 48>
   LMS_BOOTSTRAP_ADMIN_EMAIL=you@yourcompany.com        # first start only: creates the platform super admin
   LMS_BOOTSTRAP_ADMIN_PASSWORD=<12+ characters>
   ```
3. `./mvnw spring-boot:run`. Hibernate runs with `ddl-auto: validate`, so it checks that every entity matches the schema and never changes the database.
4. Optional: `python scripts/api_smoke_test.py` against a fresh database.

## API (v1)
Every endpoint requires `Authorization: Bearer <token>` except sign-in (`/auth/login`, `/auth/mfa/verify`, `/auth/refresh`),
password reset (`/auth/password/forgot|reset`), `/auth/email/verify`, and `GET /public/**`.

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/login` (`tenantCode` omitted for platform staff) · `POST /auth/mfa/verify` · `POST /auth/refresh` · `POST /auth/logout` · `GET /auth/me` · `POST /auth/password` |
| Sessions | `GET /auth/sessions` · `DELETE /auth/sessions/{id}` · `GET /auth/login-history` |
| MFA | `GET /auth/mfa` · `POST /auth/mfa/totp/setup|enable` · `POST /auth/mfa/backup-codes` · `POST /auth/mfa/disable` |
| Recovery | `POST /auth/password/forgot|reset` · `POST /auth/email/verify/request` · `POST /auth/email/verify` |
| Account admin | `GET/POST /users/{id}/lock` · `POST /users/{id}/unlock` · `POST /users/{id}/mfa/reset` · `GET/POST /users/{id}/logins` · `DELETE /users/{id}/logins/{loginId}` · `GET /users/{id}/login-history` · `GET /users/{id}/accessibility|preferences` |
| My settings | `GET/PATCH /me/accessibility` · `GET/PATCH /me/preferences` |
| Accommodations | `GET/POST /users/{id}/accommodations` · `PATCH /accommodations/{id}` · `GET /courses/{id}/accommodations` |
| Audit log | `GET /audit-log` (filters: `actorUserId`, `affectedUserId`, `actionType`, `resourceType`, `resourceId`, `from`, `to`; `tenantId` for platform admins) |
| Platform (staff) | `POST/GET /platform/tenants` · `GET/PATCH /platform/tenants/{id}` · `GET/PUT /platform/tenants/{id}/subscription|user-limits` · `GET/PATCH /platform/tenants/{id}/institution` · `GET/POST /platform/tenants/{id}/domains` · `POST .../domains/{domainId}/verify|mark-verified|primary` · `DELETE .../domains/{domainId}` · `GET /platform/users/{id}/platform-profile` · `POST/GET /platform/users` · `PATCH /platform/users/{id}/platform-profile` |
| Support access | `POST/GET /platform/support-access` · `POST /platform/support-access/{id}/approve|deny|start|end` |
| Users | `POST/GET /users` (filters: `type`, `status`, `q`, paging) · `GET/PATCH /users/{id}` · `POST /users/{id}/deactivate|reactivate|suspend` · `PUT /users/{id}/password` |
| Profiles | `GET/PATCH /users/{id}/profile` · `/student-profile` · `/employee-profile` · `/advisor-profile` |
| Roles | `GET/POST /roles` · `GET/PATCH /roles/{id}` · `GET/PUT /roles/{id}/permissions` · `GET /permissions` · `GET/POST /users/{id}/roles` · `DELETE /users/{id}/roles/{userRoleId}` |
| Observers | `POST /observer-links` · `POST /observer-links/{id}/revoke` · `GET /users/{id}/observers` · `GET /users/{id}/observing` |
| Institution | `GET /tenant` · `GET/PATCH /tenant/settings` · `GET/PUT /tenant/branding` · `GET /tenant/subscription` · `GET /tenant/user-limits` · `GET/PATCH /tenant/institution` · `GET/POST /tenant/domains` · `POST /tenant/domains/{id}/verify|primary` · `DELETE /tenant/domains/{id}` |
| Public (no token) | `GET /public/tenants/{code}/branding` · `GET /public/domains/{domain}/branding` (verified domains only) |
| Academic structure | `GET/POST /org-units` · `PATCH /org-units/{id}` · `GET/POST /terms` · `GET /terms/current` · `PATCH /terms/{id}` · `GET/POST /course-categories` · `PATCH /course-categories/{id}` |
| Courses | `POST/GET /courses` (filters: `termId`, `status`, `categoryId`, `orgUnitId`, `q`) · `GET /courses/mine` · `GET/PATCH /courses/{id}` · `POST /courses/{id}/publish|unpublish|conclude|archive|unarchive` · `PUT /courses/{id}/self-enrollment` |
| Sections | `GET/POST /courses/{id}/sections` · `GET/PATCH /sections/{id}` · `POST /sections/{id}/crosslist|uncrosslist` · `GET/POST /sections/{id}/staff` · `DELETE /sections/{id}/staff/{staffId}` |
| Enrollment | `GET/POST /sections/{id}/enrollments` · `POST /enrollments/self` (code) · `GET /enrollments/{id}` · `PATCH /enrollments/{id}/status` · `GET /users/{id}/enrollments` |
| Waitlist | `GET/POST /sections/{id}/waitlist` · `GET /waitlist/mine` · `POST /waitlist/{id}/accept|decline|leave` |
| Groups | `GET/POST /courses/{id}/group-sets` · `GET/POST /group-sets/{id}/groups` · `GET/POST /groups/{id}/members` · `DELETE /groups/{id}/members/{userId}` |
| Content types | `GET/POST /content-types` |
| Modules | `GET/POST /courses/{id}/modules` · `PUT /courses/{id}/modules/order` · `GET/PATCH/DELETE /modules/{id}` · `POST /modules/{id}/publish[?includeItems=true]|unpublish` · `PUT /modules/{id}/prerequisites` · `POST /modules/{id}/requirements` · `DELETE /modules/{id}/requirements/{reqId}` · `PUT /modules/{id}/items/order` · `GET /modules/{id}/progress` |
| Assignments | `GET/POST /courses/{id}/assignments` · `GET/PATCH/DELETE /assignments/{id}` · `POST /assignments/{id}/publish|unpublish|move` · `GET/POST /assignments/{id}/overrides` · `DELETE /assignments/{id}/overrides/{overrideId}` |
| Submissions | `POST /assignments/{id}/submissions` (text/link, `draft`) · `GET /assignments/{id}/submissions/mine` · `GET /assignments/{id}/submissions` (graders) · `POST /assignments/{id}/post-grades` · `PUT/DELETE /assignments/{id}/students/{userId}/excuse` · `GET /submissions/{id}` · `POST /submissions/{id}/grade|reassign` |
| Peer reviews | `POST /assignments/{id}/peer-reviews/assign` · `GET /assignments/{id}/peer-reviews[/mine|/received]` · `POST /peer-reviews/{id}/submit` |
| Comment library | `GET/POST /comment-library` · `POST /comment-library/{id}/use` · `DELETE /comment-library/{id}` |
| Content | `GET/POST /courses/{id}/content` (filter: `type`) · `GET /courses/{id}/front-page` · `GET/PATCH/DELETE /content/{id}` · `POST /content/{id}/publish|unpublish|move` · `POST/DELETE /content/{id}/done` · `GET /content/{id}/progress` |

Authorization: institution admins (`TENANT_ADMIN`) can do everything in their institution. Other users need permissions
granted through roles (`USER_READ`, `USER_MANAGE`, `ROLE_MANAGE`, `OBSERVER_MANAGE`, `TENANT_SETTINGS_MANAGE`,
`ACADEMIC_STRUCTURE_MANAGE`, `COURSE_READ`, `COURSE_MANAGE`, `ENROLLMENT_MANAGE`, `AUDIT_READ`, `ACCOMMODATION_MANAGE`). Everyone can read and edit their own basic profile.
Permissions are read at sign-in, so a newly assigned role applies from the next sign-in.

Course-level rights come from section staff assignments, as in Canvas and Blackboard (`security/CourseAccess`):
- **Teachers** (PRIMARY, CO_INSTRUCTOR) edit, publish and conclude their course, manage its roster, and create groups.
  Catalog fields (code, term, credits, seats ...) need `COURSE_MANAGE`.
- **TAs, graders and reviewers** see the course and the roster.
- **Students and observers** see a course only while it is published or concluded and they are enrolled.

Seats: active STUDENT, AUDIT and VISITING enrollments take a seat (observers do not), and so does an open waitlist offer.
The section row is locked while seats change, so parallel enrollments cannot overfill a section.
When a seat frees up (withdrawal, more capacity, a declined offer) the next waitlisted student is enrolled automatically,
or offered the seat if they asked for an offer.

Modules work like Canvas modules (`serviceimpl/ModuleProgressServiceImplementation`):
- **Locks:** a module is LOCKED for a student while the teacher locks it, its unlock date is in the future, or a
  prerequisite (an earlier module) is not completed.
- **Progress:** an unlocked module is UNLOCKED, then STARTED, then COMPLETED once every requirement is met. Requirements
  are VIEW or MARK_DONE on an item. A module without requirements counts as completed.
- **Sequential progress:** every item after the first unmet requirement stays locked.
- **What students see:** only published modules and items. INSTRUCTOR_ONLY items stay hidden, and modules for another
  section are not shown. Locked items come back without their body and with a `lockReason`.
- Opening an item records a view. Progress is kept in `student_content_progress` and `student_module_progress`.
- **Module items** are content and assignments in one order. Assignments take SUBMIT or MIN_SCORE (points, posted
  grades only) requirements; an excused assignment counts as met.

Assignments follow Canvas:
- **Dates:** an assignment is due at the end of its due day in the course time zone. Overrides give a student, section
  or group their own dates or extra attempts; the most generous applicable one wins.
- **Handing in:** students submit text (TEXT_ENTRY, ESSAY, CODE_SUBMISSION) or a link (URL_SUBMISSION), or save a draft.
  Late work is marked LATE, work not handed in after the due date shows as MISSING, and `lateSubmissionAllowed=false`
  closes it at the due date. In a group assignment one member submits for the group, and one grade covers the group
  unless `gradeGroupIndividually`.
- **Grading:** teachers, TAs and graders grade (reviewers cannot). With the course's AUTOMATIC post policy students see
  grades at once; with MANUAL they see them after `post-grades`. Graders can excuse a student or reassign an attempt
  (it stops counting toward the attempt limit). Anonymous grading hides names from graders.

## Sign-in and account security
- **Sessions:** sign-in returns a short-lived access token and a refresh token (30 days, `lms.security.refresh-ttl-days`).
  Each refresh rotates the refresh token. Only its SHA-256 hash is stored, in `user_session`. Access tokens carry the
  session id, so signing out, ending a device's session, suspending, locking or resetting a password stops them at once
  (`401 SESSION_ENDED`).
- **MFA:** authenticator apps (TOTP, RFC 6238) plus 10 one-time backup codes. With MFA on, the password step returns
  `mfaRequired` and a 5-minute `mfaToken`, and `/auth/mfa/verify` finishes sign-in. The secret is stored encrypted. Users
  flagged `mfa_required` (platform staff) or tenants that require MFA get `mfaSetupRequired` and cannot turn it off.
  Admins can reset a user's MFA (lost phone).
- **Locks:** 5 wrong passwords or codes lock the account for 15 minutes. Admins can lock (indefinitely or until a date)
  and unlock.
- **Password reset and email verification:** single-use links (30 minutes and 24 hours), stored as hashes. Forgot
  password always answers 202, so it does not reveal which accounts exist. Mail goes through `MailService`; the
  development sender only logs it.
- **Domains:** an institution adds its domains; each gets a token to publish as a DNS TXT record
  (`lms-verification=<token>` on the domain or on `_lms-verification.<domain>`), then asks for verification. Platform
  admins can add domains as verified or mark them verified. Only verified domains can be primary or find the
  institution's sign-in branding.
- **Extra sign-in names:** `user_login` (for example a SIS id) work like a username.
- **Plan limits:** `tenant_user_limit` (or the plan's maximum) caps active students, instructors and admins.
  Creating or reactivating beyond it gives `409 USER_LIMIT_REACHED`.
- **Audit log:** user creation, status changes, role changes, password changes and resets, MFA, locks, logins and
  accommodations are recorded in `audit_log` with the actor, IP and user agent.
- **Accommodations:** disability services (`ACCOMMODATION_MANAGE`) approve them for students. A course's teachers see
  the active ones for their students, without the details when they are confidential.

## After a schema change
```
psql -d lms-app -At -f scripts/catalog.sql > scripts/catalog.json
python scripts/gen_entities.py
```
Never edit generated entities or enums by hand. Put custom queries in the hand-written repositories listed in `gen_entities.py`
(the generator never overwrites those).

## Not done yet
- Services and controllers for the other domains: gradebook, quizzes, discussions, etc. The entities and repositories for them already exist.
- In academic structure and enrollment, not built yet: course prerequisites, meeting schedules, blueprint courses, course navigation, differentiation tags, observer pairing codes, group leaders.
- In content, not built yet: content blocks, interactive/SCORM details, per-user content permissions (RESTRICTED items stay hidden), release conditions, mastery paths, course pacing, file uploads. Quiz and discussion module requirements arrive with those features.
- In assignments, not built yet: file, media and annotation submissions (needs file storage), rubrics, moderated grading,
  plagiarism scans, submission annotations, feedback tags, automatic late-penalty deduction, section-limited graders,
  and feeding grades into the gradebook (next step).
- Waitlist offers do not expire on their own yet (needs a scheduled job).
- SSO (SAML/OIDC) sign-in flows; SMS and email MFA codes (TOTP only for now).
- Sending real email (plug an SMTP implementation into `MailService`).
- Applying accommodations automatically (extended time on quizzes arrives with quizzes).
