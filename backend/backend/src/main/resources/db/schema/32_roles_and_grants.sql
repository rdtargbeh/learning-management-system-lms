-- ================================================================================
-- 32. APPLICATION ROLE, GRANTS, POST-DEPLOY NOTES
-- ================================================================================
-- Run last.
SET search_path TO lms_core, public;
-- ================================================================================
-- APPLICATION ROLE
-- ================================================================================
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'lms_app') THEN
    CREATE ROLE lms_app NOLOGIN;
  END IF;
END
$$;
GRANT USAGE ON SCHEMA lms_core TO lms_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA lms_core TO lms_app;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA lms_core TO lms_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA lms_core GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO lms_app;
-- Create a login user and grant it lms_app, e.g.:
--   CREATE ROLE lms_api LOGIN PASSWORD '...' IN ROLE lms_app;


-- ================================================================================
-- POST-DEPLOY NOTES
-- ================================================================================
-- 1) Seed: system roles (ADMIN, INSTRUCTOR, TA, STUDENT, OBSERVER, DESIGNER),
--    permissions, assessment_type and content_type rows, default grade_scale.
-- 2) Partition high-volume, append-only tables by month in production:
--    activity_log, learning_event, xapi_statement, audit_log, notification,
--    integration_log, error_log, video_watch_progress.
-- 3) Use Flyway or Liquibase for every change after this baseline.

