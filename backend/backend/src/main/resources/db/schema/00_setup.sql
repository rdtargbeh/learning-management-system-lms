-- ================================================================================
-- 00. SETUP: schema, extensions, helper functions
-- ================================================================================
-- Run first. Run order is in run_all.sql.
SET search_path TO lms_core, public;

--       SET LOCAL app.current_tenant_id = '<tenant uuid>';
--       ... queries ...
--       COMMIT;
--   With no tenant set, tenant tables return zero rows (safe default).
--   Also set the signed-in user:  SET LOCAL app.current_user_id = '<user uuid>';
--   Platform operators: SET LOCAL app.is_platform_admin = 'on' (use a separate DB role).
--
-- Users (Identity, Accounts & Profiles domain)
--   * users is the only place accounts are created.
--   * is_system_user = TRUE  -> platform staff: tenant_id NULL, user_type 'PLATFORM'
--     is_system_user = FALSE -> institution user: tenant_id required
--   * Inserting a user creates user_profile + the profile for its user_type automatically.
--   * Tenant data that belongs to a person (enrollment, grades, ...) references
--     users(tenant_id, user_id), so it can only point at users of the same tenant.
--     Users with academic records cannot be hard-deleted: set account_status = 'DEACTIVATED'.
--   Connect the app as role lms_app (created at the end). Table owners are subject to
--   RLS too because FORCE ROW LEVEL SECURITY is enabled.
-- ================================================================================


CREATE SCHEMA IF NOT EXISTS lms_core;
SET search_path TO lms_core, public;

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS citext;     -- case-insensitive email
CREATE EXTENSION IF NOT EXISTS pg_trgm;    -- fuzzy search indexes
CREATE EXTENSION IF NOT EXISTS btree_gin;  -- composite GIN indexes

-- --------------------------------------------------------------------------------
-- Helper functions
-- --------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION app_current_tenant_id() RETURNS UUID
LANGUAGE sql STABLE AS $$
  SELECT NULLIF(current_setting('app.current_tenant_id', true), '')::uuid
$$;

CREATE OR REPLACE FUNCTION app_is_platform_admin() RETURNS BOOLEAN
LANGUAGE sql STABLE AS $$
  SELECT COALESCE(NULLIF(current_setting('app.is_platform_admin', true), '')::boolean, false)
$$;

CREATE OR REPLACE FUNCTION app_current_user_id() RETURNS UUID
LANGUAGE sql STABLE AS $$
  SELECT NULLIF(current_setting('app.current_user_id', true), '')::uuid
$$;

CREATE OR REPLACE FUNCTION set_date_updated() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  NEW.date_updated := now();
  RETURN NEW;
END;
$$;


-- --------------------------------------------------------------------------------
-- Trigger functions (the triggers themselves are in each domain file)
-- --------------------------------------------------------------------------------
-- A student cannot sit on the waitlist of a section they are already enrolled in
-- (replaces the original CHECK-with-subquery, which PostgreSQL does not allow).
CREATE OR REPLACE FUNCTION waitlist_not_enrolled() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF EXISTS (SELECT 1 FROM enrollment e
             WHERE e.section_id = NEW.section_id AND e.user_id = NEW.user_id
               AND e.enrollment_status = 'ACTIVE') THEN
    RAISE EXCEPTION 'User % is already enrolled in section %', NEW.user_id, NEW.section_id;
  END IF;
  RETURN NEW;
END;
$$;

-- Every score change on a grade entry is written to grade_history automatically.
CREATE OR REPLACE FUNCTION grade_entry_history() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.score IS DISTINCT FROM OLD.score OR NEW.percentage IS DISTINCT FROM OLD.percentage THEN
    INSERT INTO grade_history (tenant_id, entry_id, previous_score, new_score,
                               previous_percentage, new_percentage, changed_by, date_changed)
    VALUES (NEW.tenant_id, NEW.entry_id, OLD.score, NEW.score,
            OLD.percentage, NEW.percentage, NEW.graded_by, now());
  END IF;
  RETURN NEW;
END;
$$;

-- --------------------------------------------------------------------------------
-- Identity: profiles are created automatically from users.user_type
-- --------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION users_create_profiles() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  INSERT INTO user_profile (tenant_id, user_id) VALUES (NEW.tenant_id, NEW.user_id)
  ON CONFLICT (user_id) DO NOTHING;

  CASE NEW.user_type
    WHEN 'PLATFORM' THEN
      INSERT INTO platform_user_profile (user_id) VALUES (NEW.user_id) ON CONFLICT (user_id) DO NOTHING;
    WHEN 'STUDENT' THEN
      INSERT INTO student_profile (tenant_id, user_id) VALUES (NEW.tenant_id, NEW.user_id) ON CONFLICT (user_id) DO NOTHING;
    WHEN 'INSTRUCTOR', 'TEACHING_ASSISTANT', 'STAFF', 'TENANT_ADMIN', 'ADVISOR' THEN
      INSERT INTO employee_profile (tenant_id, user_id, employee_type, employment_type, academic_rank)
      VALUES (NEW.tenant_id, NEW.user_id,
              CASE NEW.user_type WHEN 'INSTRUCTOR' THEN 'FACULTY'
                                 WHEN 'TEACHING_ASSISTANT' THEN 'TEACHING_ASSISTANT'
                                 WHEN 'ADVISOR' THEN 'ACADEMIC_ADVISOR'
                                 WHEN 'TENANT_ADMIN' THEN 'ADMINISTRATOR'
                                 ELSE 'STAFF' END,
              CASE WHEN NEW.user_type = 'TEACHING_ASSISTANT' THEN 'GRADUATE_ASSISTANT' ELSE 'FULL_TIME' END,
              CASE WHEN NEW.user_type = 'TEACHING_ASSISTANT' THEN 'TEACHING_ASSISTANT' END)
      ON CONFLICT (user_id) DO NOTHING;
      IF NEW.user_type = 'ADVISOR' THEN
        INSERT INTO academic_advisor_profile (tenant_id, user_id) VALUES (NEW.tenant_id, NEW.user_id)
        ON CONFLICT (user_id) DO NOTHING;
      END IF;
    WHEN 'OBSERVER' THEN
      NULL;  -- observers only need users + user_profile; access is granted in observer_student_link
  END CASE;
  RETURN NEW;
END;
$$;

-- A user can never move between platform and tenant, or between tenants.
CREATE OR REPLACE FUNCTION users_lock_tenant() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.tenant_id IS DISTINCT FROM OLD.tenant_id OR NEW.is_system_user <> OLD.is_system_user THEN
    RAISE EXCEPTION 'users.tenant_id and is_system_user cannot be changed (user %)', OLD.user_id;
  END IF;
  RETURN NEW;
END;
$$;

-- User-owned rows (profile, sessions, MFA, preferences, ...) take tenant_id from their user.
-- SECURITY DEFINER so it works during sign-in, before a tenant is set on the session.
CREATE OR REPLACE FUNCTION inherit_user_tenant() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = lms_core, public AS $$
BEGIN
  IF NEW.user_id IS NOT NULL THEN
    SELECT u.tenant_id INTO NEW.tenant_id FROM users u WHERE u.user_id = NEW.user_id;
  END IF;
  RETURN NEW;
END;
$$;

-- Role assignments: platform roles only for platform users; tenant roles only inside the user's tenant.
CREATE OR REPLACE FUNCTION check_user_role() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = lms_core, public AS $$
DECLARE u_system BOOLEAN; u_tenant UUID; r_level VARCHAR; r_tenant UUID;
BEGIN
  SELECT is_system_user, tenant_id INTO u_system, u_tenant FROM users WHERE user_id = NEW.user_id;
  SELECT role_level, tenant_id INTO r_level, r_tenant FROM roles WHERE role_id = NEW.role_id;
  IF r_level = 'PLATFORM' AND NOT u_system THEN
    RAISE EXCEPTION 'Platform role % can only be assigned to a platform (system) user', NEW.role_id;
  ELSIF r_level = 'TENANT' AND u_system THEN
    RAISE EXCEPTION 'Tenant role % cannot be assigned to a platform user; use platform_support_access', NEW.role_id;
  ELSIF r_level = 'TENANT' AND r_tenant IS NOT NULL AND r_tenant <> u_tenant THEN
    RAISE EXCEPTION 'Role % belongs to another tenant', NEW.role_id;
  END IF;
  NEW.role_scope := CASE WHEN r_level = 'PLATFORM' THEN 'PLATFORM' ELSE COALESCE(NULLIF(NEW.role_scope, 'PLATFORM'), 'TENANT') END;
  RETURN NEW;
END;
$$;

-- TABLE OF CONTENTS (264 tables)
--   1. Tenancy & Institution Management (7): tenant, institution, tenant_settings, tenant_branding, tenant_domain, tenant_subscription, tenant_user_limit
--   2. Identity, Accounts & Profiles (23): users, user_profile, roles, permissions, role_permissions, user_role, user_session, login_history, password_reset_token, mfa_configuration, email_verification_token, account_lock, audit_log, student_profile, platform_user_profile, employee_profile, academic_advisor_profile, observer_student_link, platform_support_access, user_accessibility_settings, user_preferences, accommodation, user_login
--   3. Academic Structure (14): course_category, course, course_section, course_module, course_instructor, course_prerequisite, course_schedule, learning_outcome, org_unit, academic_term, blueprint_association, blueprint_content_lock, blueprint_sync_run, course_navigation_item
--   4. Enrollment Management (9): enrollment, waitlist, student_group, group_members, group_leader, group_set, differentiation_tag, differentiation_tag_member, observer_pairing_code
--   5. Learning Content Management (16): content_type, content, content_block, video_lecture, interactive_content, content_permission, system_resource_library, module_prerequisite, module_completion_requirement, student_module_progress, release_condition, mastery_path_rule, mastery_path_range, mastery_path_set_item, course_pace, course_pace_item
--   6. E-Book Platform (11): ebook, ebook_file, ebook_chapter, ebook_chapter_assignment, user_ebook_access, ebook_reading_progress, ebook_annotation, ebook_chapter_assessment, user_chapter_assessment_attempt, ebook_usage_analytics, ebook_search
--   7. Content Delivery & Learning Activity (5): content_delivery_tracking, student_content_progress, video_watch_progress, learning_event, student_learning_session
--   8. Assessment & Testing (16): assessment_type, assessment, assessment_blueprint, question_bank, question, question_option, assessment_attempt, student_answer, assessment_submission, item_analysis, test_security, proctoring_session, assessment_question_group, question_bank_share, assessment_attempt_event, question_regrade
--   9. Assignments & Coursework (10): assignment, assignment_submission, assignment_group, assignment_peer_review, feedback, feedback_tag, assignment_override, plagiarism_scan, submission_annotation, comment_library_item
--  10. Grading & Evaluation (22): grade_scale, grade_scale_item, grade_category, grade_item, gradebook, grade_entry, grade_history, rubric_template, rubric_criteria, rubric_level, rubric_assessment, rubric_criteria_score, late_policy, moderation_grader, provisional_grade, grader_assignment, grading_period_group, grading_period, gradebook_period_grade, gradebook_custom_column, gradebook_custom_column_value, final_grade_submission
--  11. Discussion & Collaboration (11): discussion, discussion_thread, discussion_post, discussion_mention, discussion_reaction, discussion_subscription, discussion_analytics, collaboration, collaboration_member, discussion_checkpoint, discussion_post_read
--  12. Messaging (5): message_thread, thread_participant, message, message_reaction, message_read_status
--  13. Announcements & Notifications (2): announcement, announcement_view
--  14. Notifications & Alerts (5): notification_template, notification, notification_preference, notification_queue, communication_channel
--  15. Calendar & Attendance (14): academic_calendar, course_calendar, calendar_event, event_participant, event_reminder, attendance_session, student_attendance, attendance_record, office_hour_slot, appointment_booking, conference_session, planner_note, planner_override, conference_participant
--  16. Learning Analytics & Reporting (8): analytics_dataset, engagement_metric, learner_success_indicator, course_health_check, report_template, custom_report, dashboard_widget, report_schedule
--  17. Gamification & Achievements (6): badge, badge_criteria, student_badge, badge_progress, achievement, leaderboard
--  18. Academic Advising & Student Support (11): advising_session, student_advice, academic_plan, student_intervention_plan, tutor_assignment, support_resource_referral, student_goal, goal_progress, mentorship_program, early_alert_rule, early_alert
--  19. Integrations & Interoperability (15): external_tool, lti_consumer, lti_provider, external_tool_usage, content_package, xapi_statement, api_key, webhook_integration, sso_configuration, integration_log, sis_integration, sis_sync_run, lti_registration, lti_resource_link, course_copy_job
--  20. Learning Standards & Compliance (10): educational_standard, standards_framework, content_standard_alignment, outcome_standard_alignment, compliance_requirement, accessibility_standard, data_privacy_policy, accreditation_metric, outcome_result, outcome_proficiency_rating
--  21. Privacy & Records (FERPA / GDPR) (4): consent_record, data_request, retention_policy, legal_hold
--  22. Knowledge Base & Institutional Content (10): learning_object_repository, learning_object, learning_object_metadata, content_audit, course_library, open_educational_resource, knowledge_base, help_article, institutional_policy, policy_version
--  23. File Management & Storage (7): file_storage, attachment, file_version, file_folder, storage_quota, accessibility_scan, alternative_format
--  24. Comments & Reactions (3): comment, comment_reaction, comment_mention
--  25. Metadata & Organization (3): tag, entity_tag, entity_version
--  26. Search & Discovery (2): search_index, saved_search
--  27. Audit & Logging (3): activity_log, system_log, compliance_audit
--  28. System Administration & Maintenance (5): system_config, scheduled_job, job_execution, error_log, feature_flag
--  29. ePortfolio & Catalog (6): portfolio, portfolio_section, portfolio_artifact, portfolio_share, catalog_listing, catalog_order
--  30. AI Features (1): ai_interaction
