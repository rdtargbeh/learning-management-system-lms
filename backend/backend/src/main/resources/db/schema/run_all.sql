-- Runs the whole schema in one transaction, in the right order.
-- From this folder:  psql -d <your_db> -v ON_ERROR_STOP=1 -f run_all.sql
\set ON_ERROR_STOP on
BEGIN;
\ir 00_setup.sql
\ir 01_tenancy_institution_management.sql
\ir 02_identity_accounts_profiles.sql
\ir 03_academic_structure.sql
\ir 04_enrollment_management.sql
\ir 05_learning_content_management.sql
\ir 06_e_book_platform.sql
\ir 07_content_delivery_learning_activity.sql
\ir 08_assessment_testing.sql
\ir 09_assignments_coursework.sql
\ir 10_grading_evaluation.sql
\ir 11_discussion_collaboration.sql
\ir 12_messaging.sql
\ir 13_announcements_notifications.sql
\ir 14_notifications_alerts.sql
\ir 15_calendar_attendance.sql
\ir 16_learning_analytics_reporting.sql
\ir 17_gamification_achievements.sql
\ir 18_academic_advising_student_support.sql
\ir 19_integrations_interoperability.sql
\ir 20_learning_standards_compliance.sql
\ir 21_privacy_records_ferpa_gdpr.sql
\ir 22_knowledge_base_institutional_content.sql
\ir 23_file_management_storage.sql
\ir 24_comments_reactions.sql
\ir 25_metadata_organization.sql
\ir 26_search_discovery.sql
\ir 27_audit_logging.sql
\ir 28_system_administration_maintenance.sql
\ir 29_eportfolio_catalog.sql
\ir 30_ai_features.sql
\ir 31_foreign_keys.sql
\ir 32_roles_and_grants.sql
COMMIT;
