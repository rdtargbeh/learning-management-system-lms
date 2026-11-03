# LMS backend

Spring Boot 3.4 / Java 21 backend for the multi-tenant Learning Management System, aligned with **schema v3.1**
(PostgreSQL, 264 tables in 30 domains).

## What's in here

The code is organised by layer. Every layer is one flat package under `learning_management_system.backend`, so a
table's classes are found by name (`Course`, `CourseDto`, `CourseMapper`, `CourseRepository`, `CourseService`,
`CourseServiceImplementation`, `CourseController`). Each implemented table has its own service, implementation and
controller (domains 01 to 04 completely). The only services without a table of their own are helpers: `AuthService`
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
| `utility/` | Small helpers: `Patch`, `ClientInfo` (IP, user agent), `DnsTxtResolver` (domain verification), `BlueprintCopyMap` (blueprint copies), `Tokens` (random tokens, SHA-256), `Totp` (RFC 6238 authenticator codes), `SecretCipher` (AES-GCM for stored MFA secrets). |
| `specification/` | JPA Specifications for dynamic search filters (`AuditLogSpecification`). |
| `src/main/resources/db/schema/` | The database schema: one SQL file per domain, plus `run_all.sql`. This is the source of truth. |
| `scripts/` | `gen_entities.py` (regenerates entities, repositories and enums), `catalog.sql`, `api_smoke_test.py` (61 identity/tenancy checks), `api_test_courses.py` (184 course/enrollment checks), `api_test_content.py` (145 content/module checks), `api_test_assignments.py` (194 assignment checks), `api_test_identity.py` (268 checks: branding, plan limits, institution record, domains, sessions, MFA, password reset, locks, audit log, profiles, role permissions, settings, accommodations). `api_test_academic.py` (255 checks: staff, prerequisites, schedules, outcomes, navigation, blueprints), `api_test_enrollment.py` (157 checks: group sets, auto-assign, leaders, tags, pairing codes), `api_test_learning.py` (322 checks: videos, content blocks, interactive items, content permissions, resource library, release conditions, mastery paths, pacing). Run them in that order on a fresh database; the identity test reads mailed tokens from the app log (`LMS_APP_LOG`), the academic test writes final grades with psql (`LMS_PSQL`). |

So far services and controllers exist for **tenancy, identity, academic structure, courses, enrollment, content, modules and assignments**.
Every table of domains 01 (tenancy), 02 (identity), 03 (academic structure) and 04 (enrollment) is now covered.

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
| Prerequisites | `GET/POST /courses/{id}/prerequisites` · `DELETE /courses/{id}/prerequisites/{pid}` · `GET /courses/{id}/prerequisites/check[?userId=]` |
| Meeting times | `GET/POST /sections/{id}/schedule` · `PUT/DELETE /sections/{id}/schedule/{scheduleId}` · `GET /me/schedule[?termId=]` |
| Outcomes | `GET/POST /courses/{id}/outcomes[?includeInherited&includeInactive]` · `GET/POST /org-units/{id}/outcomes` · `GET/PATCH/DELETE /outcomes/{id}` |
| Navigation | `GET/PUT /courses/{id}/navigation` |
| Blueprints | `GET/POST /courses/{id}/blueprint/associations` · `DELETE .../associations/{aid}` · `GET/PUT /courses/{id}/blueprint/locks` · `DELETE .../locks/{lockId}` · `GET/POST /courses/{id}/blueprint/syncs` · `GET .../syncs/{runId}` · `GET /courses/{id}/blueprint-status` |
| Enrollment | `GET/POST /sections/{id}/enrollments` · `POST /enrollments/self` (code) · `GET /enrollments/{id}` · `PATCH /enrollments/{id}/status` · `GET /users/{id}/enrollments` |
| Waitlist | `GET/POST /sections/{id}/waitlist` · `GET /waitlist/mine` · `POST /waitlist/{id}/accept|decline|leave` |
| Groups | `GET/POST /courses/{id}/group-sets` · `GET/PATCH/DELETE /group-sets/{id}` · `POST /group-sets/{id}/auto-assign` · `GET/POST /group-sets/{id}/groups` · `GET/PATCH/DELETE /groups/{id}` · `GET/POST /groups/{id}/members` · `DELETE /groups/{id}/members/{userId}` · `GET /courses/{id}/groups/mine` · `GET/POST /groups/{id}/leaders` · `POST /groups/{id}/leaders/{leaderId}/end` |
| Tags | `GET/POST /courses/{id}/tags` · `PATCH/DELETE /tags/{id}` · `GET/POST /tags/{id}/members` · `DELETE /tags/{id}/members/{userId}` |
| Pairing codes | `GET/POST /users/{id}/pairing-codes` · `POST /observer-links/pair` |
| Content types | `GET/POST /content-types` |
| Modules | `GET/POST /courses/{id}/modules` · `PUT /courses/{id}/modules/order` · `GET/PATCH/DELETE /modules/{id}` · `POST /modules/{id}/publish[?includeItems=true]|unpublish` · `GET/PUT /modules/{id}/prerequisites` · `GET/POST /modules/{id}/requirements` · `DELETE /modules/{id}/requirements/{reqId}` · `PUT /modules/{id}/items/order` · `GET /modules/{id}/progress` |
| Assignments | `GET/POST /courses/{id}/assignments` · `GET/PATCH/DELETE /assignments/{id}` · `POST /assignments/{id}/publish|unpublish|move` · `GET/POST /assignments/{id}/overrides` · `DELETE /assignments/{id}/overrides/{overrideId}` |
| Submissions | `POST /assignments/{id}/submissions` (text/link, `draft`) · `GET /assignments/{id}/submissions/mine` · `GET /assignments/{id}/submissions` (graders) · `POST /assignments/{id}/post-grades` · `PUT/DELETE /assignments/{id}/students/{userId}/excuse` · `GET /submissions/{id}` · `POST /submissions/{id}/grade|reassign` |
| Peer reviews | `POST /assignments/{id}/peer-reviews/assign` · `GET /assignments/{id}/peer-reviews[/mine|/received]` · `POST /peer-reviews/{id}/submit` |
| Comment library | `GET/POST /comment-library` · `POST /comment-library/{id}/use` · `DELETE /comment-library/{id}` |
| Content | `GET/POST /courses/{id}/content` (filter: `type`) · `GET /courses/{id}/front-page` · `GET/PATCH/DELETE /content/{id}` · `POST /content/{id}/publish|unpublish|move` · `POST/DELETE /content/{id}/done` · `GET /content/{id}/progress` |
| Content details | `GET/PUT/DELETE /content/{id}/video` · `GET/PUT/DELETE /content/{id}/interactive` · `GET/POST /content/{id}/blocks` · `PUT /content/{id}/blocks/order` · `PATCH/DELETE /content-blocks/{id}` |
| Content permissions | `GET/POST /content/{id}/permissions` · `DELETE /content/{id}/permissions/{pid}` · `GET /content/{id}/permissions/mine` |
| Resource library | `GET/POST /resource-library[?type&q]` · `GET/PATCH/DELETE /resource-library/{id}` · `POST /resource-library/{id}/approve|unapprove|use|download` |
| Release conditions | `GET/POST /courses/{id}/release-conditions[?targetType&targetId]` · `DELETE /courses/{id}/release-conditions/{cid}` |
| Mastery paths | `GET/POST /courses/{id}/mastery-paths` · `GET/DELETE /mastery-paths/{id}` · `GET /mastery-paths/{id}/mine` · `POST /mastery-paths/{id}/ranges` · `PATCH/DELETE /mastery-path-ranges/{id}` · `POST /mastery-path-ranges/{id}/items` · `DELETE /mastery-path-items/{id}` |
| Course pacing | `GET/POST /courses/{id}/paces` · `GET /courses/{id}/pace/mine` · `GET/PATCH/DELETE /paces/{id}` · `POST /paces/{id}/refresh|publish|archive` · `GET /paces/{id}/schedule[?userId=]` · `GET /paces/{id}/items` · `PATCH /pace-items/{id}` |

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

Modules work like Canvas modules (`serviceimpl/StudentModuleProgressServiceImplementation`):
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

More learning content:
- **Content blocks:** a page can be built from ordered blocks (text, code, table ...); students read them unless the
  page is locked. Interactive, SCORM and external-tool items carry launch details: LTI needs a tool, SCORM a package,
  embeds and links an https URL.
- **Content permissions:** students get VIEW, DOWNLOAD, PRINT and COMMENT on normal items. RESTRICTED items need a VIEW
  grant (to the user or one of their roles). Grants and denials can target a role or a user; a user's own row wins.
  INSTRUCTOR_ONLY items are never shown to students.
- **Resource library:** platform resources (platform admins, approved at once) and institution resources (suggested by
  staff, approved with `COURSE_MANAGE`). Students see only approved public ones. Use and download are counted.
- **Release conditions (Brightspace-style):** on content, modules and assignments. Types: DATE, MEMBER (user,
  section, group or tag), SUBMITTED and PERFORMANCE (min % on an assignment, posted grades only), VIEWED, COMPLETED.
  Conditions in one group must all be met; any one group releases the item. Students get a reason such as
  `Submit "X" first`. A locked module locks all its items. Staff are never held back.
- **Mastery paths (Canvas-style):** score ranges on a points assignment, each releasing items. Ranges may touch but not
  overlap (`409 RANGES_OVERLAP`); the top range includes 100%. Items on a path stay locked until the student's posted
  score falls in a range that holds them.
- **Course pacing (Canvas-style):** a pace for the course, a section or one student (most specific wins) gives each
  module item a number of days. Dates start at the student's enrollment (or the course start), skip weekends and
  blackout days, and are squeezed to fit before the pace's end date. A published pace sets assignment due dates.

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

## Academic structure rules
- **Prerequisites:** REQUIRED courses must be completed (with at least `minGrade` when set, from the final or override
  letter in the gradebook); COREQUISITE courses completed or taken now; RECOMMENDED are shown only. Checked when
  students enroll themselves or join a waitlist and when teachers enroll students (`409 PREREQUISITES_NOT_MET`).
  Admins with `ENROLLMENT_MANAGE` can pass `overridePrerequisites`. A REQUIRED chain cannot loop.
- **Meeting times:** course admins set them. A room or instructor cannot be double-booked in the same term
  (`ROOM_CONFLICT`, `INSTRUCTOR_CONFLICT`). `/me/schedule` is the weekly timetable from enrollments and teaching.
- **Outcomes:** course outcomes (course teachers) and org-unit outcomes (`ACADEMIC_STRUCTURE_MANAGE`), inherited
  down the org-unit tree. DECAYING_AVERAGE takes a weight 1-99 (default 65), N_MASTERY a count 1-10 (default 5).
  An outcome with student results cannot be deleted, only deactivated.
- **Navigation:** built-in tools plus external tools; teachers reorder, rename and hide. Home cannot be hidden;
  Settings is last and staff-only.
- **Groups:** a student is in at most one group per set. Auto-assign puts unassigned students into the smallest
  groups at random, creating groups as needed (per section when the set is restricted to sections). Leaders live in
  `group_leader`; with `autoAssignLeader` the first member becomes PROJECT_LEAD and the lead passes on when they leave.
  A set used by a group assignment, or a group that handed in work, cannot be deleted.
- **Differentiation tags:** staff-only student labels; assignment overrides can target a tag (TAG), so tagged students
  get their own dates. Students never see tags.
- **Pairing codes:** a student (or OBSERVER_MANAGE) creates an 8-character code valid 7 days, stored only as a hash; an
  observer account redeems it once to follow the student. A new code replaces unused ones.
- **Blueprints (Canvas-style):** a BLUEPRINT course is associated with STANDARD courses (one blueprint each). A sync
  copies modules (with prerequisites and requirements), pages (with video details and content blocks) and assignments, and optionally the
  course settings. Locks per item cover content, points, due dates, availability and settings; a SETTINGS lock covers
  the course settings. In associated courses locked aspects cannot be edited (`409 BLUEPRINT_LOCKED`) and are always
  overwritten by a sync. Unlocked ones are overwritten only when the course has not changed the item since the last
  sync, otherwise the course's version stays and the run lists an exception. Items deleted from the blueprint are
  deleted from courses unless changed there or students handed in work. Which copy belongs to which blueprint item is
  kept in each run's `changes` (internal `copies` map, not returned by the API).

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

## Schema fixes
`blueprint_sync_run.run_status` and `sis_sync_run.run_status` were `VARCHAR(20)`, too short for their own allowed
values `COMPLETED_WITH_EXCEPTIONS` and `COMPLETED_WITH_ERRORS`. The schema files now use `VARCHAR(30)`. A database
created before this fix needs:
```
ALTER TABLE lms_core.blueprint_sync_run ALTER COLUMN run_status TYPE VARCHAR(30);
ALTER TABLE lms_core.sis_sync_run ALTER COLUMN run_status TYPE VARCHAR(30);
```

## After a schema change
```
psql -d lms-app -At -f scripts/catalog.sql > scripts/catalog.json
python scripts/gen_entities.py
```
Never edit generated entities or enums by hand. Put custom queries in the hand-written repositories listed in `gen_entities.py`
(the generator never overwrites those).

## Not done yet
- Services and controllers for the other domains: gradebook, quizzes, discussions, etc. The entities and repositories for them already exist.
- Blueprints: quizzes, discussions, files, announcements and rubrics are not synced yet (they arrive with those features); `notifyUsers` is stored but no notification is sent yet.
- In content, not built yet: file uploads; quiz and discussion requirements, release conditions and mastery-path triggers arrive with those features (FILE release targets too).
- In assignments, not built yet: file, media and annotation submissions (needs file storage), rubrics, moderated grading,
  plagiarism scans, submission annotations, feedback tags, automatic late-penalty deduction, section-limited graders,
  and feeding grades into the gradebook (next step).
- Waitlist offers do not expire on their own yet (needs a scheduled job).
- SSO (SAML/OIDC) sign-in flows; SMS and email MFA codes (TOTP only for now).
- Sending real email (plug an SMTP implementation into `MailService`).
- Applying accommodations automatically (extended time on quizzes arrives with quizzes).
