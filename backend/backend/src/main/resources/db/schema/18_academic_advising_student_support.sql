-- ================================================================================
-- 18. ACADEMIC ADVISING & STUDENT SUPPORT (11 tables)
-- ================================================================================
-- Tables: advising_session, student_advice, academic_plan, student_intervention_plan, tutor_assignment, support_resource_referral, student_goal, goal_progress, mentorship_program, early_alert_rule, early_alert
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- AdvisingSession: Student advising appointments
CREATE TABLE advising_session (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    student_id UUID NOT NULL,
    advisor_id UUID NOT NULL,
    session_type VARCHAR(50) NOT NULL,
    session_status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    session_title VARCHAR(255),
    session_description TEXT,
    session_notes TEXT,
    session_date DATE NOT NULL,
    session_start_time TIME,
    session_end_time TIME,
    duration_minutes INT,
    location VARCHAR(255),
    is_online BOOLEAN DEFAULT FALSE,
    meeting_url VARCHAR(500),
    is_mandatory BOOLEAN DEFAULT FALSE,
    student_attended BOOLEAN,
    date_attended TIMESTAMPTZ,
    follow_up_required BOOLEAN DEFAULT FALSE,
    follow_up_date DATE,
    topics_discussed JSONB,
    action_items JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_advising_session_type CHECK (session_type IN ('ACADEMIC_PLANNING', 'COURSE_SELECTION', 'DEGREE_PROGRESS', 'CAREER_PLANNING', 'REMEDIAL', 'CRISIS_INTERVENTION', 'PROBATION_REVIEW', 'REGISTRATION_HOLD')),
    CONSTRAINT ck_advising_session_status CHECK (session_status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW', 'RESCHEDULED')),
    CONSTRAINT ck_advising_session_times CHECK (session_end_time IS NULL OR session_end_time > session_start_time)
);

-- StudentAdvice: Specific advice given to students
CREATE TABLE student_advice (
    advice_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    session_id UUID NOT NULL,
    advisor_id UUID NOT NULL,
    student_id UUID NOT NULL,
    advice_category VARCHAR(50) NOT NULL,
    advice_title VARCHAR(255) NOT NULL,
    advice_description TEXT NOT NULL,
    advice_detail JSONB,
    is_critical BOOLEAN DEFAULT FALSE,
    implementation_deadline DATE,
    status VARCHAR(50) DEFAULT 'PENDING',
    student_acknowledged BOOLEAN DEFAULT FALSE,
    date_acknowledged TIMESTAMPTZ,
    outcome_status VARCHAR(50),
    outcome_date TIMESTAMPTZ,
    outcome_notes TEXT,
    is_archived BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_student_advice_category CHECK (advice_category IN ('ACADEMIC', 'CAREER', 'PERSONAL', 'FINANCIAL', 'HEALTH', 'REGISTRATION', 'REQUIREMENT', 'WARNING', 'OPPORTUNITY')),
    CONSTRAINT ck_student_advice_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'DISMISSED', 'DEFERRED')),
    CONSTRAINT ck_student_advice_outcome CHECK (outcome_status IS NULL OR outcome_status IN ('SUCCESSFUL', 'PARTIAL', 'UNSUCCESSFUL', 'EXPIRED'))
);

-- AcademicPlan: Student's degree/academic plan
CREATE TABLE academic_plan (
    plan_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    student_id UUID NOT NULL UNIQUE,
    advisor_id UUID NOT NULL,
    degree_id UUID,
    major_id UUID,
    minor_id UUID,
    concentration_id UUID,
    plan_name VARCHAR(255),
    plan_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_date DATE NOT NULL,
    approved_date DATE,
    approved_by UUID,
    total_credits_required INT,
    credits_completed INT DEFAULT 0,
    gpa_requirement NUMERIC(3,2),
    completion_target_date DATE,
    expected_graduation_semester VARCHAR(20),
    plan_content JSONB,
    curriculum_version VARCHAR(20),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_academic_plan_status CHECK (plan_status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'ARCHIVED')),
    CONSTRAINT ck_academic_plan_credits CHECK (credits_completed >= 0 AND (total_credits_required IS NULL OR credits_completed <= total_credits_required))
);

-- StudentInterventionPlan: Targeted intervention plans
CREATE TABLE student_intervention_plan (
    plan_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    student_id UUID NOT NULL,
    advisor_id UUID NOT NULL,
    intervention_type VARCHAR(50) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    intervention_title VARCHAR(255) NOT NULL,
    intervention_description TEXT,
    target_metric VARCHAR(100),
    target_value NUMERIC(8,2),
    start_date DATE NOT NULL,
    end_date DATE,
    plan_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    monitored_metrics JSONB,
    interventions_assigned JSONB,
    check_in_frequency VARCHAR(50),
    last_check_in DATE,
    next_check_in DATE,
    effectiveness_rating NUMERIC(3,1),
    effectiveness_notes TEXT,
    is_archived BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_student_intervention_type CHECK (intervention_type IN ('ACADEMIC_SUPPORT', 'MENTAL_HEALTH', 'FINANCIAL', 'TIME_MANAGEMENT', 'STUDY_SKILLS', 'MOTIVATION', 'ATTENDANCE', 'SUBSTANCE_ABUSE', 'BEHAVIORAL')),
    CONSTRAINT ck_student_intervention_risk CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT ck_student_intervention_status CHECK (plan_status IN ('ACTIVE', 'PAUSED', 'COMPLETED', 'DISCONTINUED')),
    CONSTRAINT ck_student_intervention_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

-- TutorAssignment: Tutoring assignments for students
CREATE TABLE tutor_assignment (
    assignment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    student_id UUID NOT NULL,
    tutor_id UUID NOT NULL,
    advisor_id UUID,
    course_id UUID,
    subject_area VARCHAR(100),
    tutoring_type VARCHAR(50) NOT NULL,
    frequency VARCHAR(50),
    sessions_scheduled INT,
    sessions_completed INT DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE,
    assignment_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    focus_areas TEXT,
    student_goals TEXT,
    assignment_notes TEXT,
    effectiveness_rating NUMERIC(3,1),
    effectiveness_notes TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_tutor_assignment_type CHECK (tutoring_type IN ('ONE_ON_ONE', 'SMALL_GROUP', 'WORKSHOP', 'ONLINE', 'PEER_TUTORING')),
    CONSTRAINT ck_tutor_assignment_frequency CHECK (frequency IN ('WEEKLY', 'BIWEEKLY', 'MONTHLY', 'AS_NEEDED')),
    CONSTRAINT ck_tutor_assignment_status CHECK (assignment_status IN ('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_tutor_assignment_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

-- SupportResourceReferral: Referrals to campus support services
CREATE TABLE support_resource_referral (
    referral_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    student_id UUID NOT NULL,
    advisor_id UUID NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_name VARCHAR(255),
    resource_description TEXT,
    resource_contact VARCHAR(255),
    resource_url VARCHAR(500),
    referral_reason TEXT,
    referral_urgency VARCHAR(50),
    referral_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    student_contacted_resource BOOLEAN DEFAULT FALSE,
    date_contacted TIMESTAMPTZ,
    resource_feedback TEXT,
    follow_up_date DATE,
    referral_notes TEXT,
    is_archived BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_support_resource_type CHECK (resource_type IN ('ACADEMIC_SUPPORT', 'MENTAL_HEALTH', 'CAREER_SERVICES', 'FINANCIAL_AID', 'DISABILITY_SERVICES', 'COUNSELING', 'HEALTH_SERVICES', 'TUTORING', 'TECHNOLOGY_SUPPORT', 'HOUSING')),
    CONSTRAINT ck_support_resource_urgency CHECK (referral_urgency IN ('ROUTINE', 'URGENT', 'EMERGENCY')),
    CONSTRAINT ck_support_resource_status CHECK (referral_status IN ('PENDING', 'REFERRED', 'ACKNOWLEDGED', 'COMPLETED', 'DECLINED', 'CLOSED'))
);

-- StudentGoal: Student-set academic and personal goals
CREATE TABLE student_goal (
    goal_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    student_id UUID NOT NULL,
    advisor_id UUID,
    goal_title VARCHAR(255) NOT NULL,
    goal_description TEXT NOT NULL,
    goal_category VARCHAR(50) NOT NULL,
    goal_type VARCHAR(50) NOT NULL,
    target_date DATE NOT NULL,
    priority VARCHAR(50) DEFAULT 'MEDIUM',
    goal_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    measurable_criteria TEXT,
    success_metrics JSONB,
    related_course_ids JSONB,
    related_resources JSONB,
    is_public BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_student_goal_category CHECK (goal_category IN ('ACADEMIC', 'CAREER', 'PERSONAL_DEVELOPMENT', 'SKILL_BUILDING', 'HEALTH', 'FINANCIAL')),
    CONSTRAINT ck_student_goal_type CHECK (goal_type IN ('SHORT_TERM', 'LONG_TERM', 'SEMESTER_GOAL', 'DEGREE_GOAL')),
    CONSTRAINT ck_student_goal_status CHECK (goal_status IN ('ACTIVE', 'PAUSED', 'ACHIEVED', 'ABANDONED')),
    CONSTRAINT ck_student_goal_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

-- GoalProgress: Track progress toward student goals
CREATE TABLE goal_progress (
    progress_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    goal_id UUID NOT NULL,
    student_id UUID NOT NULL,
    progress_percentage NUMERIC(5,2),
    progress_notes TEXT,
    milestone_description VARCHAR(255),
    milestone_date DATE,
    evidence_of_progress TEXT,
    advisor_feedback TEXT,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_goal_progress_percentage CHECK (progress_percentage >= 0 AND progress_percentage <= 100)
);

-- MentorshipProgram: Mentorship relationships and programs
CREATE TABLE mentorship_program (
    program_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    program_name VARCHAR(255) NOT NULL,
    program_description TEXT,
    program_type VARCHAR(50) NOT NULL,
    mentor_qualifications TEXT,
    mentee_requirements TEXT,
    duration_weeks INT,
    capacity INT,
    current_matches INT DEFAULT 0,
    start_date DATE,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    meeting_frequency VARCHAR(50),
    program_goals JSONB,
    success_metrics JSONB,
    coordinator_id UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_mentorship_program_name CHECK (LENGTH(program_name) >= 3),
    CONSTRAINT ck_mentorship_program_type CHECK (program_type IN ('PEER_MENTORING', 'FACULTY_MENTORING', 'ALUMNI_MENTORING', 'CAREER_MENTORING', 'PEER_LEADER', 'FIRST_GENERATION')),
    CONSTRAINT ck_mentorship_program_capacity CHECK (capacity IS NULL OR capacity > 0),
    CONSTRAINT ck_mentorship_program_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE TABLE early_alert_rule (
    alert_rule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID,
    org_unit_id UUID,
    rule_name VARCHAR(255) NOT NULL,
    rule_type VARCHAR(30) NOT NULL,
    threshold_value NUMERIC(8,2),
    threshold_days INT,
    grade_item_id UUID,
    notify_advisor BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_early_alert_rule_type CHECK (rule_type IN ('GRADE_BELOW','COURSE_GRADE_BELOW','NO_LOGIN_DAYS','MISSED_DUE_DATES','ABSENCES','LOW_ACTIVITY','MANUAL'))
);

CREATE TABLE early_alert (
    early_alert_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    alert_rule_id UUID,
    course_id UUID,
    user_id UUID NOT NULL,
    raised_by UUID,
    alert_reason TEXT,
    alert_status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    intervention_plan_id UUID,
    resolved_by UUID,
    date_resolved TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_early_alert_status CHECK (alert_status IN ('OPEN','ACKNOWLEDGED','IN_PROGRESS','RESOLVED','DISMISSED'))
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_advising_session_tenant ON advising_session(tenant_id);
CREATE INDEX idx_advising_session_student ON advising_session(student_id);
CREATE INDEX idx_advising_session_advisor ON advising_session(advisor_id);
CREATE INDEX idx_advising_session_date ON advising_session(session_date);
CREATE INDEX idx_advising_session_status ON advising_session(session_status);
CREATE INDEX idx_advising_session_type ON advising_session(session_type);
CREATE INDEX idx_student_advice_session ON student_advice(session_id);
CREATE INDEX idx_student_advice_advisor ON student_advice(advisor_id);
CREATE INDEX idx_student_advice_student ON student_advice(student_id);
CREATE INDEX idx_student_advice_status ON student_advice(status);
CREATE INDEX idx_student_advice_category ON student_advice(advice_category);
CREATE INDEX idx_academic_plan_advisor ON academic_plan(advisor_id);
CREATE INDEX idx_academic_plan_status ON academic_plan(plan_status);
CREATE INDEX idx_academic_plan_graduation ON academic_plan(expected_graduation_semester);
CREATE INDEX idx_student_intervention_plan_student ON student_intervention_plan(student_id);
CREATE INDEX idx_student_intervention_plan_advisor ON student_intervention_plan(advisor_id);
CREATE INDEX idx_student_intervention_plan_status ON student_intervention_plan(plan_status);
CREATE INDEX idx_student_intervention_plan_type ON student_intervention_plan(intervention_type);
CREATE INDEX idx_tutor_assignment_student ON tutor_assignment(student_id);
CREATE INDEX idx_tutor_assignment_tutor ON tutor_assignment(tutor_id);
CREATE INDEX idx_tutor_assignment_course ON tutor_assignment(course_id);
CREATE INDEX idx_tutor_assignment_status ON tutor_assignment(assignment_status);
CREATE INDEX idx_support_resource_referral_student ON support_resource_referral(student_id);
CREATE INDEX idx_support_resource_referral_advisor ON support_resource_referral(advisor_id);
CREATE INDEX idx_support_resource_referral_type ON support_resource_referral(resource_type);
CREATE INDEX idx_support_resource_referral_status ON support_resource_referral(referral_status);
CREATE INDEX idx_student_goal_student ON student_goal(student_id);
CREATE INDEX idx_student_goal_advisor ON student_goal(advisor_id);
CREATE INDEX idx_student_goal_status ON student_goal(goal_status);
CREATE INDEX idx_student_goal_target_date ON student_goal(target_date);
CREATE INDEX idx_goal_progress_goal ON goal_progress(goal_id);
CREATE INDEX idx_goal_progress_student ON goal_progress(student_id);
CREATE INDEX idx_goal_progress_date_updated ON goal_progress(date_updated DESC);
CREATE INDEX idx_mentorship_program_tenant ON mentorship_program(tenant_id);
CREATE INDEX idx_mentorship_program_type ON mentorship_program(program_type);
CREATE INDEX idx_mentorship_program_is_active ON mentorship_program(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_mentorship_program_coordinator ON mentorship_program(coordinator_id);
CREATE INDEX idx_student_advice_tenant ON student_advice(tenant_id);
CREATE INDEX idx_academic_plan_tenant ON academic_plan(tenant_id);
CREATE INDEX idx_student_intervention_plan_tenant ON student_intervention_plan(tenant_id);
CREATE INDEX idx_tutor_assignment_tenant ON tutor_assignment(tenant_id);
CREATE INDEX idx_support_resource_referral_tenant ON support_resource_referral(tenant_id);
CREATE INDEX idx_student_goal_tenant ON student_goal(tenant_id);
CREATE INDEX idx_goal_progress_tenant ON goal_progress(tenant_id);
CREATE INDEX idx_early_alert_open ON early_alert(course_id, alert_status) WHERE alert_status IN ('OPEN','ACKNOWLEDGED','IN_PROGRESS');
CREATE INDEX idx_academic_plan_approved_by_fk ON academic_plan(approved_by);
CREATE INDEX idx_tutor_assignment_advisor_id_fk ON tutor_assignment(advisor_id);
CREATE INDEX idx_early_alert_alert_rule_id_fk ON early_alert(alert_rule_id);
CREATE INDEX idx_early_alert_intervention_plan_id_fk ON early_alert(intervention_plan_id);
CREATE INDEX idx_early_alert_raised_by_fk ON early_alert(raised_by);
CREATE INDEX idx_early_alert_resolved_by_fk ON early_alert(resolved_by);
CREATE INDEX idx_early_alert_rule_course_id_fk ON early_alert_rule(course_id);
CREATE INDEX idx_early_alert_rule_created_by_fk ON early_alert_rule(created_by);
CREATE INDEX idx_early_alert_rule_grade_item_id_fk ON early_alert_rule(grade_item_id);
CREATE INDEX idx_early_alert_rule_org_unit_id_fk ON early_alert_rule(org_unit_id);
CREATE INDEX idx_early_alert_rule_tenant_id_fk ON early_alert_rule(tenant_id);
CREATE INDEX idx_early_alert_tenant_id_fk ON early_alert(tenant_id);
CREATE INDEX idx_early_alert_user_id_fk ON early_alert(user_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_advising_session_date_updated BEFORE UPDATE ON advising_session FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_student_advice_date_updated BEFORE UPDATE ON student_advice FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_academic_plan_date_updated BEFORE UPDATE ON academic_plan FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_student_intervention_plan_date_updated BEFORE UPDATE ON student_intervention_plan FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_tutor_assignment_date_updated BEFORE UPDATE ON tutor_assignment FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_support_resource_referral_date_updated BEFORE UPDATE ON support_resource_referral FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_student_goal_date_updated BEFORE UPDATE ON student_goal FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_goal_progress_date_updated BEFORE UPDATE ON goal_progress FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_mentorship_program_date_updated BEFORE UPDATE ON mentorship_program FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_early_alert_rule_date_updated BEFORE UPDATE ON early_alert_rule FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_early_alert_date_updated BEFORE UPDATE ON early_alert FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE advising_session ENABLE ROW LEVEL SECURITY;
ALTER TABLE advising_session FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_advising_session_tenant ON advising_session FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE student_advice ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_advice FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_student_advice_tenant ON student_advice FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE academic_plan ENABLE ROW LEVEL SECURITY;
ALTER TABLE academic_plan FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_academic_plan_tenant ON academic_plan FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE student_intervention_plan ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_intervention_plan FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_student_intervention_plan_tenant ON student_intervention_plan FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE tutor_assignment ENABLE ROW LEVEL SECURITY;
ALTER TABLE tutor_assignment FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_tutor_assignment_tenant ON tutor_assignment FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE support_resource_referral ENABLE ROW LEVEL SECURITY;
ALTER TABLE support_resource_referral FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_support_resource_referral_tenant ON support_resource_referral FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE student_goal ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_goal FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_student_goal_tenant ON student_goal FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE goal_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE goal_progress FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_goal_progress_tenant ON goal_progress FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE mentorship_program ENABLE ROW LEVEL SECURITY;
ALTER TABLE mentorship_program FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_mentorship_program_tenant ON mentorship_program FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE early_alert_rule ENABLE ROW LEVEL SECURITY;
ALTER TABLE early_alert_rule FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_early_alert_rule_tenant ON early_alert_rule FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE early_alert ENABLE ROW LEVEL SECURITY;
ALTER TABLE early_alert FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_early_alert_tenant ON early_alert FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
