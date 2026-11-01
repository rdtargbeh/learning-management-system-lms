# LMS database schema by domain

PostgreSQL 14+ · schema `lms_core` · 264 tables in 30 domains
See `FEATURE_PARITY.md` for coverage against Canvas and Blackboard Ultra.

## Run it
From this folder:

```
psql -d <your_db> -v ON_ERROR_STOP=1 -f run_all.sql
```

`run_all.sql` runs every file in order inside one transaction, so a failure leaves the database untouched.

## Order and why it matters
| File | Contents |
|---|---|
| `00_setup.sql` | Schema, extensions, helper functions (tenant and user context) and all trigger functions. Run first. |
| `01_…` to `30_…` | One file per domain: its tables, indexes, triggers and row-level security policies. |
| `31_foreign_keys.sql` | All foreign keys, grouped by domain. Kept separate because domains reference each other, so they can only be added once every table exists. |
| `32_roles_and_grants.sql` | The `lms_app` role, grants and post-deploy notes. Run last. |

Domain files 01–30 don't depend on each other, so you can edit or reload one without touching the rest. If you add a table, put its foreign keys in `31_foreign_keys.sql` under its domain heading.

## Users: platform vs tenant (domain 02)
`users` is the only place accounts are created.

| | Platform user | Tenant (institution) user |
|---|---|---|
| `is_system_user` | `TRUE` | `FALSE` |
| `tenant_id` | `NULL` | required |
| `user_type` | `PLATFORM` | `TENANT_ADMIN`, `STAFF`, `INSTRUCTOR`, `TEACHING_ASSISTANT`, `ADVISOR`, `STUDENT`, `OBSERVER` |
| Email / username unique | across the platform | within the tenant |
| Profile created automatically | `platform_user_profile` | by `user_type`: STUDENT gets `student_profile`; INSTRUCTOR, TEACHING_ASSISTANT, STAFF and TENANT_ADMIN get `employee_profile`; ADVISOR gets `employee_profile` + `academic_advisor_profile`; OBSERVER gets none |
| MFA | required | per tenant policy |

- Every user also gets a `user_profile` (name, pronouns, avatar, language, time zone).
- **Profile tables:** `user_profile` (everyone: name, pronouns, avatar, language), `platform_user_profile` (platform staff), `student_profile` (academic record), `employee_profile` (faculty, TAs, advisors, staff, institution admins, told apart by `employee_type`), and `academic_advisor_profile` (advising-only extras for advisors, who also have an `employee_profile`).
- **Observers** (parents, mentors, athletics advisors) have no profile of their own. `observer_student_link` records who may follow which student, the relationship, the access level and the consent. Any user can be an observer, including an existing employee.
- A person can hold extra roles by adding another profile row, for example a graduate student who is also a TA.
- A user can never move between tenants, or between platform and tenant.
- Platform staff never get tenant roles. They enter a tenant through `platform_support_access`, which is time-boxed, has a reason, and is linked to `audit_log`.
- Tenant data about a person (enrollments, grades, submissions, attendance, …) references `users(tenant_id, user_id)`. It can only point at users of the same institution.
- A user who has academic records can't be hard-deleted. Set `account_status = 'DEACTIVATED'` instead.

## Session context
Every transaction sets who and where:

```
BEGIN;
SET LOCAL app.current_tenant_id = '<tenant uuid>';   -- omit for platform-only work
SET LOCAL app.current_user_id   = '<user uuid>';
-- SET LOCAL app.is_platform_admin = 'on';           -- platform operators only
-- queries
COMMIT;
```

With no tenant set, tenant tables return no rows. Platform rows (users, profiles and sessions with `tenant_id NULL`) are visible only to platform admins and to the user themselves.
