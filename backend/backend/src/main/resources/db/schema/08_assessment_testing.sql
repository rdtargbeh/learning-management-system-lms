-- ================================================================================
-- 08. ASSESSMENT & TESTING (16 tables)
-- ================================================================================
-- Tables: assessment_type, assessment, assessment_blueprint, question_bank, question, question_option, assessment_attempt, student_answer, assessment_submission, item_analysis, test_security, proctoring_session, assessment_question_group, question_bank_share, assessment_attempt_event, question_regrade
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- AssessmentType: Types of assessments
CREATE TABLE assessment_type (
    type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    type_name VARCHAR(100) NOT NULL UNIQUE,
    type_code VARCHAR(50) UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_assessment_type_name CHECK (LENGTH(type_name) >= 3)
);

-- Assessment: Quiz, exam, test definitions
CREATE TABLE assessment (
    assessment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    module_id UUID,
    type_id UUID NOT NULL,
    assessment_title VARCHAR(255) NOT NULL,
    assessment_description TEXT,
    instructions TEXT,
    assessment_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT DEFAULT 0,
    time_limit_minutes INT,
    shuffle_questions BOOLEAN DEFAULT FALSE,
    shuffle_options BOOLEAN DEFAULT FALSE,
    show_one_question_at_time BOOLEAN DEFAULT TRUE,
    allow_review BOOLEAN DEFAULT TRUE,
    show_correct_answers BOOLEAN DEFAULT TRUE,
    show_score_immediately BOOLEAN DEFAULT FALSE,
    allow_multiple_attempts BOOLEAN DEFAULT TRUE,
    max_attempts INT,
    scoring_type VARCHAR(50) DEFAULT 'HIGHEST_SCORE',
    pass_score NUMERIC(5,2) DEFAULT 70.0,
    total_points NUMERIC(8,2),
    display_points BOOLEAN DEFAULT TRUE,
    due_date DATE,
    date_available DATE,
    date_close DATE,
    create_for_group BOOLEAN DEFAULT FALSE,
    is_anonymous BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_assessment_title CHECK (LENGTH(assessment_title) >= 3),
    CONSTRAINT ck_assessment_status CHECK (assessment_status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED', 'DISABLED')),
    CONSTRAINT ck_assessment_time_limit CHECK (time_limit_minutes IS NULL OR time_limit_minutes > 0),
    CONSTRAINT ck_assessment_attempts CHECK (max_attempts IS NULL OR max_attempts > 0),
    CONSTRAINT ck_assessment_pass_score CHECK (pass_score >= 0 AND pass_score <= 100),
    CONSTRAINT ck_assessment_dates CHECK (date_close IS NULL OR date_close >= date_available)
);

-- AssessmentBlueprint: Test blueprint (question distribution plan)
CREATE TABLE assessment_blueprint (
    blueprint_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assessment_id UUID NOT NULL UNIQUE,
    total_questions INT NOT NULL,
    total_points NUMERIC(8,2) NOT NULL,
    blueprint_description TEXT,
    distribution_plan JSONB,
    cognitive_levels JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_assessment_blueprint_questions CHECK (total_questions > 0),
    CONSTRAINT ck_assessment_blueprint_points CHECK (total_points > 0)
);

-- QuestionBank: Repository of questions (reusable across assessments)
CREATE TABLE question_bank (
    bank_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID,
    bank_name VARCHAR(255) NOT NULL,
    bank_description TEXT,
    owner_id UUID,
    is_shared BOOLEAN DEFAULT FALSE,
    is_public BOOLEAN DEFAULT FALSE,
    total_questions INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_question_bank_name CHECK (LENGTH(bank_name) >= 3)
);

-- Question: Individual assessment questions
CREATE TABLE question (
    question_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assessment_id UUID,
    bank_id UUID,
    parent_question_id UUID,
    question_number INT,
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    difficulty_level VARCHAR(50),
    bloom_level VARCHAR(50),
    points NUMERIC(8,2),
    partial_credit_mode VARCHAR(20) NOT NULL DEFAULT 'NONE',
    correct_answer TEXT,
    correct_answers JSONB,
    interaction_data JSONB,
    feedback_if_correct TEXT,
    feedback_if_incorrect TEXT,
    explanation TEXT,
    display_order INT DEFAULT 0,
    is_required BOOLEAN DEFAULT TRUE,
    is_randomized BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_question_text CHECK (LENGTH(question_text) >= 5),
    CONSTRAINT ck_question_difficulty CHECK (difficulty_level IS NULL OR difficulty_level IN ('EASY', 'MEDIUM', 'HARD')),
    CONSTRAINT ck_question_bloom CHECK (bloom_level IS NULL OR bloom_level IN ('REMEMBER', 'UNDERSTAND', 'APPLY', 'ANALYZE', 'EVALUATE', 'CREATE')),
    CONSTRAINT ck_question_type CHECK (question_type IN ('MULTIPLE_CHOICE','TRUE_FALSE','SHORT_ANSWER','ESSAY','MATCHING','FILL_BLANK','MULTIPLE_SELECT','NUMERIC','HOTSPOT','ORDERING','CATEGORIZATION','FORMULA','FILE_UPLOAD','STIMULUS','TEXT_ONLY','MULTIPLE_DROPDOWNS','JUMBLED_SENTENCE','EITHER_OR','LIKERT','CALCULATED_NUMERIC')),
    CONSTRAINT ck_question_partial_credit CHECK (partial_credit_mode IN ('NONE','PROPORTIONAL','RIGHT_MINUS_WRONG','CUSTOM'))
);

-- QuestionOption: Answer options for multiple choice questions
CREATE TABLE question_option (
    option_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    question_id UUID NOT NULL,
    option_letter VARCHAR(5),
    option_text TEXT NOT NULL,
    option_order INT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE,
    feedback TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_question_option UNIQUE (question_id, option_letter),
    CONSTRAINT ck_question_option_text CHECK (LENGTH(option_text) >= 1)
);

-- AssessmentAttempt: Student's test attempt
CREATE TABLE assessment_attempt (
    attempt_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assessment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    attempt_number INT NOT NULL,
    date_started TIMESTAMPTZ NOT NULL,
    date_submitted TIMESTAMPTZ,
    date_completed TIMESTAMPTZ,
    attempt_status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    score NUMERIC(5,2),
    percentage NUMERIC(5,2),
    is_passed BOOLEAN,
    time_spent_minutes INT,
    ip_address INET,
    user_agent TEXT,
    device_type VARCHAR(50),
    browser VARCHAR(100),
    session_id UUID,
    flagged_for_review BOOLEAN DEFAULT FALSE,
    suspicious_activity_detected BOOLEAN DEFAULT FALSE,
    review_notes TEXT,
    date_reviewed TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_assessment_attempt UNIQUE (assessment_id, user_id, attempt_number),
    CONSTRAINT ck_assessment_attempt_status CHECK (attempt_status IN ('IN_PROGRESS', 'SUBMITTED', 'GRADED', 'ABANDONED')),
    CONSTRAINT ck_assessment_attempt_score CHECK (score IS NULL OR (score >= 0 AND score <= 100)),
    CONSTRAINT ck_assessment_attempt_percentage CHECK (percentage IS NULL OR (percentage >= 0 AND percentage <= 100))
);

-- StudentAnswer: Individual student answers to questions
CREATE TABLE student_answer (
    answer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    attempt_id UUID NOT NULL,
    question_id UUID NOT NULL,
    option_id UUID,
    text_answer TEXT,
    numeric_answer NUMERIC(20,4),
    is_marked_for_review BOOLEAN DEFAULT FALSE,
    answer_time_seconds INT,
    is_correct BOOLEAN,
    points_earned NUMERIC(8,2),
    graded_by UUID,
    grading_notes TEXT,
    date_answered TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_graded TIMESTAMPTZ
);

-- AssessmentSubmission: Overall submission tracking (for appeal/regrade)
CREATE TABLE assessment_submission (
    submission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    attempt_id UUID NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    assessment_id UUID NOT NULL,
    submission_status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    graded_at TIMESTAMPTZ,
    final_score NUMERIC(5,2),
    final_percentage NUMERIC(5,2),
    appeal_requested BOOLEAN DEFAULT FALSE,
    appeal_reason TEXT,
    appeal_requested_at TIMESTAMPTZ,
    appeal_reviewed_at TIMESTAMPTZ,
    appeal_decision TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_assessment_submission_status CHECK (submission_status IN ('SUBMITTED', 'UNDER_REVIEW', 'APPEALED', 'REGRADES_REQUESTED', 'RESOLVED'))
);

-- ItemAnalysis: Question-level analytics for instructors
CREATE TABLE item_analysis (
    analysis_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assessment_id UUID NOT NULL,
    question_id UUID NOT NULL,
    total_attempts INT DEFAULT 0,
    correct_answers INT DEFAULT 0,
    incorrect_answers INT DEFAULT 0,
    skipped_count INT DEFAULT 0,
    difficulty_index NUMERIC(5,2),
    discrimination_index NUMERIC(5,2),
    avg_time_seconds NUMERIC(8,2),
    point_biserial_correlation NUMERIC(5,3),
    is_flagged_for_review BOOLEAN DEFAULT FALSE,
    flag_reason TEXT,
    date_analyzed TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_item_analysis UNIQUE (assessment_id, question_id),
    CONSTRAINT ck_item_analysis_difficulty CHECK (difficulty_index IS NULL OR (difficulty_index >= 0 AND difficulty_index <= 100)),
    CONSTRAINT ck_item_analysis_discrimination CHECK (discrimination_index IS NULL OR (discrimination_index >= -1 AND discrimination_index <= 1))
);

-- TestSecurity: Test security and proctoring
CREATE TABLE test_security (
    security_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assessment_id UUID NOT NULL UNIQUE,
    require_proctoring BOOLEAN DEFAULT FALSE,
    proctoring_type VARCHAR(50),
    proctoring_provider VARCHAR(100),
    require_webcam BOOLEAN DEFAULT FALSE,
    require_microphone BOOLEAN DEFAULT FALSE,
    lock_questions_after_answering BOOLEAN DEFAULT FALSE,
    disable_copy_paste BOOLEAN DEFAULT FALSE,
    disable_right_click BOOLEAN DEFAULT FALSE,
    disable_printing BOOLEAN DEFAULT FALSE,
    fullscreen_required BOOLEAN DEFAULT FALSE,
    window_security BOOLEAN DEFAULT FALSE,
    require_unique_session_id BOOLEAN DEFAULT FALSE,
    ip_restriction_enabled BOOLEAN DEFAULT FALSE,
    allowed_ip_addresses JSONB,
    require_authentication BOOLEAN DEFAULT TRUE,
    authentication_method VARCHAR(50),
    access_code_hash VARCHAR(255),
    require_lockdown_browser BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_test_security_proctoring CHECK (require_proctoring = FALSE OR proctoring_type IS NOT NULL),
    CONSTRAINT ck_test_security_proctoring_type CHECK (proctoring_type IS NULL OR proctoring_type IN ('AI_PROCTORED', 'HUMAN_PROCTORED', 'LIVE_PROCTORED'))
);

-- PROCTORING SESSION (runtime record; configuration lives in test_security)
CREATE TABLE proctoring_session (
    proctoring_session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    attempt_id UUID NOT NULL,
    user_id UUID NOT NULL,
    provider VARCHAR(50),
    provider_session_ref VARCHAR(255),
    session_status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    date_start TIMESTAMPTZ,
    date_end TIMESTAMPTZ,
    recording_file_id UUID,
    violation_count INT NOT NULL DEFAULT 0,
    violations JSONB,
    review_status VARCHAR(20) DEFAULT 'PENDING',
    reviewed_by UUID,
    date_reviewed TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_proctoring_session_status CHECK (session_status IN ('INITIATED','ACTIVE','COMPLETED','TERMINATED','FAILED')),
    CONSTRAINT ck_proctoring_session_review CHECK (review_status IS NULL OR review_status IN ('PENDING','CLEARED','FLAGGED','ESCALATED')),
    CONSTRAINT ck_proctoring_session_violations CHECK (violation_count >= 0)
);

-- QUIZ QUESTION GROUPS: pull N random questions from a bank (Canvas question groups / Blackboard pools)
CREATE TABLE assessment_question_group (
    question_group_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assessment_id UUID NOT NULL,
    bank_id UUID,
    group_name VARCHAR(255),
    pick_count INT NOT NULL DEFAULT 1,
    points_per_question NUMERIC(8,2) NOT NULL DEFAULT 1,
    display_order INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_assessment_question_group_pick CHECK (pick_count > 0),
    CONSTRAINT ck_assessment_question_group_points CHECK (points_per_question >= 0)
);

CREATE TABLE question_bank_share (
    bank_share_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    bank_id UUID NOT NULL,
    shared_with_user_id UUID,
    shared_with_course_id UUID,
    shared_with_org_unit_id UUID,
    permission_level VARCHAR(10) NOT NULL DEFAULT 'VIEW',
    shared_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_question_bank_share_one CHECK (num_nonnulls(shared_with_user_id, shared_with_course_id, shared_with_org_unit_id) = 1),
    CONSTRAINT ck_question_bank_share_level CHECK (permission_level IN ('VIEW','EDIT','OWNER'))
);

CREATE TABLE assessment_attempt_event (
    attempt_event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    attempt_id UUID NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    question_id UUID,
    event_data JSONB,
    client_timestamp TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_assessment_attempt_event_type CHECK (event_type IN ('SESSION_STARTED','SESSION_RESUMED','QUESTION_VIEWED','QUESTION_ANSWERED', 'QUESTION_FLAGGED','PAGE_BLURRED','PAGE_FOCUSED','COPY','PASTE','SUBMITTED','AUTO_SUBMITTED','TIME_EXTENDED','IP_CHANGED'))
);

CREATE TABLE question_regrade (
    regrade_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    question_id UUID NOT NULL,
    regrade_option VARCHAR(30) NOT NULL,
    previous_correct_answer JSONB,
    requested_by UUID,
    attempts_affected INT NOT NULL DEFAULT 0,
    date_applied TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_question_regrade_option CHECK (regrade_option IN ('AWARD_BOTH','ONLY_NEW_CORRECT','FULL_CREDIT_ALL','NO_REGRADE'))
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_assessment_type_is_active ON assessment_type(is_active);
CREATE INDEX idx_assessment_course ON assessment(course_id);
CREATE INDEX idx_assessment_module ON assessment(module_id);
CREATE INDEX idx_assessment_type ON assessment(type_id);
CREATE INDEX idx_assessment_is_published ON assessment(is_published);
CREATE INDEX idx_assessment_status ON assessment(assessment_status);
CREATE INDEX idx_assessment_due_date ON assessment(due_date);
CREATE INDEX idx_question_bank_tenant ON question_bank(tenant_id);
CREATE INDEX idx_question_bank_course ON question_bank(course_id);
CREATE INDEX idx_question_bank_owner ON question_bank(owner_id);
CREATE INDEX idx_question_bank_is_shared ON question_bank(is_shared);
CREATE INDEX idx_question_assessment ON question(assessment_id);
CREATE INDEX idx_question_bank ON question(bank_id);
CREATE INDEX idx_question_type ON question(question_type);
CREATE INDEX idx_question_display_order ON question(assessment_id, display_order);
CREATE INDEX idx_question_difficulty ON question(difficulty_level);
CREATE INDEX idx_question_option_question ON question_option(question_id);
CREATE INDEX idx_question_option_is_correct ON question_option(is_correct);
CREATE INDEX idx_assessment_attempt_assessment ON assessment_attempt(assessment_id);
CREATE INDEX idx_assessment_attempt_user ON assessment_attempt(user_id);
CREATE INDEX idx_assessment_attempt_status ON assessment_attempt(attempt_status);
CREATE INDEX idx_assessment_attempt_date_started ON assessment_attempt(date_started DESC);
CREATE INDEX idx_assessment_attempt_is_passed ON assessment_attempt(is_passed);
CREATE INDEX idx_student_answer_attempt ON student_answer(attempt_id);
CREATE INDEX idx_student_answer_question ON student_answer(question_id);
CREATE INDEX idx_student_answer_is_correct ON student_answer(is_correct);
CREATE INDEX idx_student_answer_marked_for_review ON student_answer(is_marked_for_review);
CREATE INDEX idx_assessment_submission_user ON assessment_submission(user_id);
CREATE INDEX idx_assessment_submission_status ON assessment_submission(submission_status);
CREATE INDEX idx_assessment_submission_appeal ON assessment_submission(appeal_requested);
CREATE INDEX idx_item_analysis_assessment ON item_analysis(assessment_id);
CREATE INDEX idx_item_analysis_question ON item_analysis(question_id);
CREATE INDEX idx_item_analysis_flagged ON item_analysis(is_flagged_for_review) WHERE is_flagged_for_review = TRUE;
CREATE INDEX idx_test_security_require_proctoring ON test_security(require_proctoring);
CREATE INDEX idx_assessment_type_tenant ON assessment_type(tenant_id);
CREATE INDEX idx_assessment_tenant ON assessment(tenant_id);
CREATE INDEX idx_assessment_blueprint_tenant ON assessment_blueprint(tenant_id);
CREATE INDEX idx_question_tenant ON question(tenant_id);
CREATE INDEX idx_question_option_tenant ON question_option(tenant_id);
CREATE INDEX idx_assessment_attempt_tenant ON assessment_attempt(tenant_id);
CREATE INDEX idx_student_answer_tenant ON student_answer(tenant_id);
CREATE INDEX idx_assessment_submission_tenant ON assessment_submission(tenant_id);
CREATE INDEX idx_item_analysis_tenant ON item_analysis(tenant_id);
CREATE INDEX idx_test_security_tenant ON test_security(tenant_id);
CREATE INDEX idx_proctoring_session_attempt ON proctoring_session(attempt_id);
CREATE INDEX idx_assessment_question_group_assessment ON assessment_question_group(assessment_id);
CREATE INDEX idx_assessment_attempt_event_attempt ON assessment_attempt_event(attempt_id, date_created);
CREATE INDEX idx_assessment_attempt_session_id_fk ON assessment_attempt(session_id);
CREATE INDEX idx_assessment_question_group_bank_id_fk ON assessment_question_group(bank_id);
CREATE INDEX idx_assessment_question_group_tenant_id_fk ON assessment_question_group(tenant_id);
CREATE INDEX idx_assessment_submission_assessment_id_fk ON assessment_submission(assessment_id);
CREATE INDEX idx_proctoring_session_recording_file_id_fk ON proctoring_session(recording_file_id);
CREATE INDEX idx_proctoring_session_reviewed_by_fk ON proctoring_session(reviewed_by);
CREATE INDEX idx_proctoring_session_tenant_id_fk ON proctoring_session(tenant_id);
CREATE INDEX idx_proctoring_session_user_id_fk ON proctoring_session(user_id);
CREATE INDEX idx_student_answer_graded_by_fk ON student_answer(graded_by);
CREATE INDEX idx_student_answer_option_id_fk ON student_answer(option_id);
CREATE INDEX idx_assessment_attempt_event_question_id_fk ON assessment_attempt_event(question_id);
CREATE INDEX idx_assessment_attempt_event_tenant_id_fk ON assessment_attempt_event(tenant_id);
CREATE INDEX idx_question_bank_share_bank_id_fk ON question_bank_share(bank_id);
CREATE INDEX idx_question_bank_share_shared_by_fk ON question_bank_share(shared_by);
CREATE INDEX idx_question_bank_share_shared_with_course_id_fk ON question_bank_share(shared_with_course_id);
CREATE INDEX idx_question_bank_share_shared_with_org_unit_id_fk ON question_bank_share(shared_with_org_unit_id);
CREATE INDEX idx_question_bank_share_shared_with_user_id_fk ON question_bank_share(shared_with_user_id);
CREATE INDEX idx_question_bank_share_tenant_id_fk ON question_bank_share(tenant_id);
CREATE INDEX idx_question_parent_question_id_fk ON question(parent_question_id);
CREATE INDEX idx_question_regrade_question_id_fk ON question_regrade(question_id);
CREATE INDEX idx_question_regrade_requested_by_fk ON question_regrade(requested_by);
CREATE INDEX idx_question_regrade_tenant_id_fk ON question_regrade(tenant_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_assessment_date_updated BEFORE UPDATE ON assessment FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_question_bank_date_updated BEFORE UPDATE ON question_bank FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_question_date_updated BEFORE UPDATE ON question FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_test_security_date_updated BEFORE UPDATE ON test_security FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_proctoring_session_date_updated BEFORE UPDATE ON proctoring_session FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_assessment_question_group_date_updated BEFORE UPDATE ON assessment_question_group FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE assessment_type ENABLE ROW LEVEL SECURITY;
ALTER TABLE assessment_type FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_assessment_type_read ON assessment_type FOR SELECT USING (tenant_id IS NULL OR tenant_id = app_current_tenant_id() OR app_is_platform_admin());
CREATE POLICY rls_assessment_type_write ON assessment_type FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE assessment ENABLE ROW LEVEL SECURITY;
ALTER TABLE assessment FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_assessment_tenant ON assessment FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE assessment_blueprint ENABLE ROW LEVEL SECURITY;
ALTER TABLE assessment_blueprint FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_assessment_blueprint_tenant ON assessment_blueprint FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE question_bank ENABLE ROW LEVEL SECURITY;
ALTER TABLE question_bank FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_question_bank_tenant ON question_bank FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE question ENABLE ROW LEVEL SECURITY;
ALTER TABLE question FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_question_tenant ON question FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE question_option ENABLE ROW LEVEL SECURITY;
ALTER TABLE question_option FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_question_option_tenant ON question_option FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE assessment_attempt ENABLE ROW LEVEL SECURITY;
ALTER TABLE assessment_attempt FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_assessment_attempt_tenant ON assessment_attempt FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE student_answer ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_answer FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_student_answer_tenant ON student_answer FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE assessment_submission ENABLE ROW LEVEL SECURITY;
ALTER TABLE assessment_submission FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_assessment_submission_tenant ON assessment_submission FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE item_analysis ENABLE ROW LEVEL SECURITY;
ALTER TABLE item_analysis FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_item_analysis_tenant ON item_analysis FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE test_security ENABLE ROW LEVEL SECURITY;
ALTER TABLE test_security FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_test_security_tenant ON test_security FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE proctoring_session ENABLE ROW LEVEL SECURITY;
ALTER TABLE proctoring_session FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_proctoring_session_tenant ON proctoring_session FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE assessment_question_group ENABLE ROW LEVEL SECURITY;
ALTER TABLE assessment_question_group FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_assessment_question_group_tenant ON assessment_question_group FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE question_bank_share ENABLE ROW LEVEL SECURITY;
ALTER TABLE question_bank_share FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_question_bank_share_tenant ON question_bank_share FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE assessment_attempt_event ENABLE ROW LEVEL SECURITY;
ALTER TABLE assessment_attempt_event FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_assessment_attempt_event_tenant ON assessment_attempt_event FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE question_regrade ENABLE ROW LEVEL SECURITY;
ALTER TABLE question_regrade FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_question_regrade_tenant ON question_regrade FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
