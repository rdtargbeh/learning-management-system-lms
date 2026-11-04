-- ================================================================================
-- 10. GRADING & EVALUATION (22 tables)
-- ================================================================================
-- Tables: grade_scale, grade_scale_item, grade_category, grade_item, gradebook, grade_entry, grade_history, rubric_template, rubric_criteria, rubric_level, rubric_assessment, rubric_criteria_score, late_policy, moderation_grader, provisional_grade, grader_assignment, grading_period_group, grading_period, gradebook_period_grade, gradebook_custom_column, gradebook_custom_column_value, final_grade_submission
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- GradeScale: Definition of grading scales (A-F, 0-100, etc.)
CREATE TABLE grade_scale (
    scale_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    scale_name VARCHAR(100) NOT NULL,
    scale_type VARCHAR(50) NOT NULL,
    description TEXT,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_grade_scale_name UNIQUE (scale_name, tenant_id),
    CONSTRAINT ck_grade_scale_type CHECK (scale_type IN ('LETTER_GRADE', 'POINTS', 'PERCENTAGE', 'GPA_SCALE', 'CUSTOM')),
    CONSTRAINT ck_grade_scale_name CHECK (LENGTH(scale_name) >= 3)
);

-- GradeScaleItem: Individual items/ranges in a grade scale
CREATE TABLE grade_scale_item (
    item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    scale_id UUID NOT NULL,
    item_order INT NOT NULL,
    item_name VARCHAR(50) NOT NULL,
    min_value NUMERIC(8,2),
    max_value NUMERIC(8,2),
    percentage_min NUMERIC(5,2),
    percentage_max NUMERIC(5,2),
    gpa_value NUMERIC(3,2),
    is_passing BOOLEAN DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_grade_scale_item_order UNIQUE (scale_id, item_order),
    CONSTRAINT ck_grade_scale_item_name CHECK (LENGTH(item_name) >= 1),
    CONSTRAINT ck_grade_scale_item_values CHECK (max_value IS NULL OR max_value >= min_value),
    CONSTRAINT ck_grade_scale_item_percent CHECK (percentage_max IS NULL OR percentage_max >= percentage_min),
    CONSTRAINT ck_grade_scale_item_gpa CHECK (gpa_value IS NULL OR (gpa_value >= 0 AND gpa_value <= 4.0))
);

-- GradeCategory: Categories of grades (homework, tests, participation, etc.)
CREATE TABLE grade_category (
    category_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    category_description TEXT,
    weight NUMERIC(5,2),
    display_order INT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    drop_lowest INT,
    drop_highest INT NOT NULL DEFAULT 0,
    never_drop_item_ids UUID[],
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_grade_category UNIQUE (course_id, category_name),
    CONSTRAINT ck_grade_category_name CHECK (LENGTH(category_name) >= 3),
    CONSTRAINT ck_grade_category_weight CHECK (weight IS NULL OR (weight >= 0 AND weight <= 100)),
    CONSTRAINT ck_grade_category_drop CHECK (drop_lowest IS NULL OR drop_lowest >= 0)
);

-- GradeItem: Individual grade items (assignments, tests, quizzes, etc.)
CREATE TABLE grade_item (
    item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    category_id UUID NOT NULL,
    assignment_id UUID,
    assessment_id UUID,
    discussion_id UUID,
    discussion_checkpoint_id UUID,
    item_name VARCHAR(255) NOT NULL,
    item_description TEXT,
    item_type VARCHAR(50) NOT NULL,
    grading_type VARCHAR(20) NOT NULL DEFAULT 'POINTS',
    points_possible NUMERIC(8,2),
    weight NUMERIC(5,2),
    calculation_formula JSONB,
    display_order INT DEFAULT 0,
    due_date DATE,
    external_id VARCHAR(255),
    external_source VARCHAR(50),
    include_in_final_grade BOOLEAN DEFAULT TRUE,
    is_extra_credit BOOLEAN DEFAULT FALSE,
    is_published BOOLEAN DEFAULT FALSE,
    is_muted BOOLEAN NOT NULL DEFAULT FALSE,
    post_policy VARCHAR(10) NOT NULL DEFAULT 'AUTOMATIC',
    date_scheduled_post TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_grade_item_name CHECK (LENGTH(item_name) >= 3),
    CONSTRAINT ck_grade_item_points CHECK (points_possible IS NULL OR points_possible > 0),
    CONSTRAINT ck_grade_item_weight CHECK (weight IS NULL OR (weight >= 0 AND weight <= 100)),
    CONSTRAINT ck_grade_item_one_source CHECK (num_nonnulls(assignment_id, assessment_id, discussion_id, discussion_checkpoint_id) <= 1),
    CONSTRAINT ck_grade_item_grading_type CHECK (grading_type IN ('POINTS','PERCENTAGE','LETTER_GRADE','GPA_SCALE','PASS_FAIL','COMPLETE_INCOMPLETE','NOT_GRADED')),
    CONSTRAINT ck_grade_item_type CHECK (item_type IN ('ASSIGNMENT','QUIZ','TEST','PARTICIPATION','ATTENDANCE','PROJECT','DISCUSSION','PEER_REVIEW','PRESENTATION','CALCULATED','EXTERNAL_TOOL','OTHER')),
    CONSTRAINT ck_grade_item_calculated CHECK (item_type <> 'CALCULATED' OR calculation_formula IS NOT NULL),
    CONSTRAINT ck_grade_item_post_policy CHECK (post_policy IN ('AUTOMATIC','MANUAL'))
);

-- Gradebook: Student's overall grade in a course
CREATE TABLE gradebook (
    gradebook_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    section_id UUID NOT NULL,
    user_id UUID NOT NULL,
    final_grade NUMERIC(8,2),
    final_grade_percentage NUMERIC(5,2),
    final_grade_letter VARCHAR(5),
    final_grade_points NUMERIC(8,2),
    current_grade NUMERIC(8,2),
    current_grade_percentage NUMERIC(5,2),
    override_percentage NUMERIC(6,2),
    override_letter VARCHAR(10),
    overridden_by UUID,
    date_overridden TIMESTAMPTZ,
    grade_scale_id UUID,
    is_locked BOOLEAN DEFAULT FALSE,
    date_locked TIMESTAMPTZ,
    locked_by UUID,
    last_grade_update TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_gradebook UNIQUE (section_id, user_id),
    CONSTRAINT ck_gradebook_final_percentage CHECK (final_grade_percentage IS NULL OR (final_grade_percentage >= 0 AND final_grade_percentage <= 100)),
    CONSTRAINT ck_gradebook_current_percentage CHECK (current_grade_percentage IS NULL OR (current_grade_percentage >= 0 AND current_grade_percentage <= 100))
);

-- GradeEntry: Individual grade entries (one per student per grade item)
CREATE TABLE grade_entry (
    entry_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    gradebook_id UUID NOT NULL,
    item_id UUID NOT NULL,
    user_id UUID NOT NULL,
    score NUMERIC(8,2),
    override_score NUMERIC(8,2),
    late_penalty_applied NUMERIC(8,2),
    points_earned NUMERIC(8,2),
    percentage NUMERIC(5,2),
    letter_grade VARCHAR(5),
    grade_date TIMESTAMPTZ,
    graded_by UUID,
    grading_notes TEXT,
    is_excluded BOOLEAN DEFAULT FALSE,
    is_incomplete BOOLEAN DEFAULT FALSE,
    is_posted BOOLEAN NOT NULL DEFAULT FALSE,
    date_posted TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_grade_entry UNIQUE (gradebook_id, item_id),
    CONSTRAINT ck_grade_entry_percentage CHECK (percentage IS NULL OR (percentage >= 0 AND percentage <= 100))
);

-- GradeHistory: Complete audit trail of grade changes
CREATE TABLE grade_history (
    history_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    entry_id UUID NOT NULL,
    previous_score NUMERIC(8,2),
    new_score NUMERIC(8,2),
    previous_percentage NUMERIC(5,2),
    new_percentage NUMERIC(5,2),
    change_reason VARCHAR(255),
    changed_by UUID NOT NULL,
    ip_address INET,
    date_changed TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- RubricTemplate: Reusable rubric templates
CREATE TABLE rubric_template (
    template_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID,
    org_unit_id UUID,
    template_name VARCHAR(255) NOT NULL,
    template_description TEXT,
    creator_id UUID,
    is_public BOOLEAN DEFAULT FALSE,
    is_shared BOOLEAN DEFAULT FALSE,
    total_points NUMERIC(8,2),
    hide_points BOOLEAN NOT NULL DEFAULT FALSE,
    free_form_comments BOOLEAN NOT NULL DEFAULT FALSE,
    rubric_type VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_rubric_template_name CHECK (LENGTH(template_name) >= 3),
    CONSTRAINT ck_rubric_template_type CHECK (rubric_type IN ('ANALYTIC', 'HOLISTIC', 'HYBRID')),
    CONSTRAINT ck_rubric_template_points CHECK (total_points IS NULL OR total_points > 0)
);

-- RubricCriteria: Criteria/dimensions in a rubric
CREATE TABLE rubric_criteria (
    criteria_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    template_id UUID NOT NULL,
    outcome_id UUID,
    criteria_name VARCHAR(255) NOT NULL,
    criteria_description TEXT,
    max_points NUMERIC(8,2) NOT NULL,
    weight NUMERIC(5,2),
    use_point_range BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_rubric_criteria_name CHECK (LENGTH(criteria_name) >= 3),
    CONSTRAINT ck_rubric_criteria_max_points CHECK (max_points > 0),
    CONSTRAINT ck_rubric_criteria_weight CHECK (weight IS NULL OR (weight >= 0 AND weight <= 100))
);

-- RubricLevel: Performance levels within criteria
CREATE TABLE rubric_level (
    level_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    criteria_id UUID NOT NULL,
    level_name VARCHAR(100) NOT NULL,
    level_description TEXT,
    points NUMERIC(8,2) NOT NULL,
    level_order INT NOT NULL,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_rubric_level_order UNIQUE (criteria_id, level_order),
    CONSTRAINT ck_rubric_level_name CHECK (LENGTH(level_name) >= 2),
    CONSTRAINT ck_rubric_level_points CHECK (points >= 0)
);

-- RubricAssessment: Application of rubric to grade an assignment
CREATE TABLE rubric_assessment (
    assessment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    grade_entry_id UUID NOT NULL UNIQUE,
    template_id UUID NOT NULL,
    assessor_id UUID NOT NULL,
    assessment_purpose VARCHAR(20) NOT NULL DEFAULT 'GRADING',
    total_score NUMERIC(8,2),
    total_percentage NUMERIC(5,2),
    assessment_feedback TEXT,
    overall_comments TEXT,
    assessment_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_rubric_assessment_percentage CHECK (total_percentage IS NULL OR (total_percentage >= 0 AND total_percentage <= 100)),
    CONSTRAINT ck_rubric_assessment_purpose CHECK (assessment_purpose IN ('GRADING','PEER_REVIEW','SELF_ASSESSMENT','PROVISIONAL'))
);

-- RubricCriteriaScore: Individual scores for each criteria in assessment
CREATE TABLE rubric_criteria_score (
    score_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assessment_id UUID NOT NULL,
    criteria_id UUID NOT NULL,
    level_id UUID NOT NULL,
    score NUMERIC(8,2),
    comments TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_rubric_criteria_score UNIQUE (assessment_id, criteria_id),
    CONSTRAINT ck_rubric_criteria_score CHECK (score IS NULL OR score >= 0)
);

CREATE TABLE late_policy (
    late_policy_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    late_deduction_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    late_deduction_percent NUMERIC(5,2) NOT NULL DEFAULT 0,
    late_deduction_interval VARCHAR(10) NOT NULL DEFAULT 'DAY',
    late_minimum_percent NUMERIC(5,2),
    missing_deduction_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    missing_deduction_percent NUMERIC(5,2) NOT NULL DEFAULT 100,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_late_policy_course UNIQUE (course_id),
    CONSTRAINT ck_late_policy_interval CHECK (late_deduction_interval IN ('HOUR','DAY')),
    CONSTRAINT ck_late_policy_percents CHECK (late_deduction_percent BETWEEN 0 AND 100 AND missing_deduction_percent BETWEEN 0 AND 100 AND (late_minimum_percent IS NULL OR late_minimum_percent BETWEEN 0 AND 100))
);

CREATE TABLE moderation_grader (
    moderation_grader_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assignment_id UUID NOT NULL,
    grader_user_id UUID NOT NULL,
    anonymous_label VARCHAR(20),
    is_final_grader BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_moderation_grader UNIQUE (assignment_id, grader_user_id)
);

CREATE TABLE provisional_grade (
    provisional_grade_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    submission_id UUID NOT NULL,
    grader_user_id UUID NOT NULL,
    score NUMERIC(8,2),
    grade_letter VARCHAR(10),
    feedback_text TEXT,
    rubric_assessment_id UUID,
    is_selected BOOLEAN NOT NULL DEFAULT FALSE,
    selected_by UUID,
    date_selected TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_provisional_grade UNIQUE (submission_id, grader_user_id)
);

CREATE TABLE grader_assignment (
    grader_assignment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assignment_id UUID,
    assessment_id UUID,
    grader_user_id UUID NOT NULL,
    section_id UUID,
    group_id UUID,
    student_user_id UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_grader_assignment_parent CHECK (num_nonnulls(assignment_id, assessment_id) = 1),
    CONSTRAINT ck_grader_assignment_scope CHECK (num_nonnulls(section_id, group_id, student_user_id) = 1)
);

CREATE TABLE grading_period_group (
    grading_period_group_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    term_id UUID,
    group_title VARCHAR(255) NOT NULL,
    is_weighted BOOLEAN NOT NULL DEFAULT FALSE,
    display_totals_for_all_periods BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE grading_period (
    grading_period_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    grading_period_group_id UUID NOT NULL,
    period_title VARCHAR(100) NOT NULL,
    date_start TIMESTAMPTZ NOT NULL,
    date_end TIMESTAMPTZ NOT NULL,
    date_close TIMESTAMPTZ NOT NULL,
    weight NUMERIC(5,2),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_grading_period_dates CHECK (date_end > date_start AND date_close >= date_end),
    CONSTRAINT ck_grading_period_weight CHECK (weight IS NULL OR weight BETWEEN 0 AND 100)
);

CREATE TABLE gradebook_period_grade (
    period_grade_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    gradebook_id UUID NOT NULL,
    grading_period_id UUID NOT NULL,
    current_score NUMERIC(6,2),
    final_score NUMERIC(6,2),
    override_score NUMERIC(6,2),
    letter_grade VARCHAR(10),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_gradebook_period_grade UNIQUE (gradebook_id, grading_period_id)
);

CREATE TABLE gradebook_custom_column (
    custom_column_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    column_title VARCHAR(255) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    is_read_only BOOLEAN NOT NULL DEFAULT FALSE,
    is_teacher_notes BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE gradebook_custom_column_value (
    custom_value_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    custom_column_id UUID NOT NULL,
    user_id UUID NOT NULL,
    value_text TEXT,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_gradebook_custom_column_value UNIQUE (custom_column_id, user_id)
);

CREATE TABLE final_grade_submission (
    grade_submission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    section_id UUID NOT NULL,
    sis_integration_id UUID,
    submitted_by UUID,
    submission_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    grade_count INT NOT NULL DEFAULT 0,
    payload JSONB,
    response JSONB,
    date_submitted TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_final_grade_submission_status CHECK (submission_status IN ('DRAFT','SUBMITTED','ACCEPTED','REJECTED','PARTIAL'))
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_grade_scale_tenant ON grade_scale(tenant_id);
CREATE INDEX idx_grade_scale_type ON grade_scale(scale_type);
CREATE INDEX idx_grade_scale_is_default ON grade_scale(is_default) WHERE is_default = TRUE;
CREATE INDEX idx_grade_scale_item_scale ON grade_scale_item(scale_id);
CREATE INDEX idx_grade_scale_item_name ON grade_scale_item(item_name);
CREATE INDEX idx_grade_category_course ON grade_category(course_id);
CREATE INDEX idx_grade_category_is_active ON grade_category(is_active);
CREATE INDEX idx_grade_category_display_order ON grade_category(course_id, display_order);
CREATE INDEX idx_grade_item_course ON grade_item(course_id);
CREATE INDEX idx_grade_item_category ON grade_item(category_id);
CREATE INDEX idx_grade_item_type ON grade_item(item_type);
CREATE INDEX idx_grade_item_due_date ON grade_item(due_date);
CREATE INDEX idx_grade_item_display_order ON grade_item(category_id, display_order);
CREATE INDEX idx_gradebook_section ON gradebook(section_id);
CREATE INDEX idx_gradebook_user ON gradebook(user_id);
CREATE INDEX idx_gradebook_final_grade ON gradebook(final_grade_percentage DESC);
CREATE INDEX idx_gradebook_is_locked ON gradebook(is_locked) WHERE is_locked = TRUE;
CREATE INDEX idx_gradebook_last_update ON gradebook(last_grade_update DESC);
CREATE INDEX idx_grade_entry_gradebook ON grade_entry(gradebook_id);
CREATE INDEX idx_grade_entry_item ON grade_entry(item_id);
CREATE INDEX idx_grade_entry_user ON grade_entry(user_id);
CREATE INDEX idx_grade_entry_grade_date ON grade_entry(grade_date DESC);
CREATE INDEX idx_grade_entry_is_excluded ON grade_entry(is_excluded) WHERE is_excluded = TRUE;
CREATE INDEX idx_grade_history_entry ON grade_history(entry_id);
CREATE INDEX idx_grade_history_changed_by ON grade_history(changed_by);
CREATE INDEX idx_grade_history_date ON grade_history(date_changed DESC);
CREATE INDEX idx_rubric_template_tenant ON rubric_template(tenant_id);
CREATE INDEX idx_rubric_template_course ON rubric_template(course_id);
CREATE INDEX idx_rubric_template_creator ON rubric_template(creator_id);
CREATE INDEX idx_rubric_template_is_public ON rubric_template(is_public) WHERE is_public = TRUE;
CREATE INDEX idx_rubric_criteria_template ON rubric_criteria(template_id);
CREATE INDEX idx_rubric_criteria_display_order ON rubric_criteria(template_id, display_order);
CREATE INDEX idx_rubric_level_criteria ON rubric_level(criteria_id);
CREATE INDEX idx_rubric_level_points ON rubric_level(points DESC);
CREATE INDEX idx_rubric_assessment_template ON rubric_assessment(template_id);
CREATE INDEX idx_rubric_assessment_assessor ON rubric_assessment(assessor_id);
CREATE INDEX idx_rubric_assessment_date ON rubric_assessment(assessment_date DESC);
CREATE INDEX idx_rubric_criteria_score_assessment ON rubric_criteria_score(assessment_id);
CREATE INDEX idx_rubric_criteria_score_criteria ON rubric_criteria_score(criteria_id);
CREATE UNIQUE INDEX uk_grade_item_assignment ON grade_item(assignment_id) WHERE assignment_id IS NOT NULL;
CREATE UNIQUE INDEX uk_grade_item_assessment ON grade_item(assessment_id) WHERE assessment_id IS NOT NULL;
CREATE INDEX idx_grade_scale_item_tenant ON grade_scale_item(tenant_id);
CREATE INDEX idx_grade_category_tenant ON grade_category(tenant_id);
CREATE INDEX idx_grade_item_tenant ON grade_item(tenant_id);
CREATE INDEX idx_gradebook_tenant ON gradebook(tenant_id);
CREATE INDEX idx_grade_entry_tenant ON grade_entry(tenant_id);
CREATE INDEX idx_grade_history_tenant ON grade_history(tenant_id);
CREATE INDEX idx_rubric_criteria_tenant ON rubric_criteria(tenant_id);
CREATE INDEX idx_rubric_level_tenant ON rubric_level(tenant_id);
CREATE INDEX idx_rubric_assessment_tenant ON rubric_assessment(tenant_id);
CREATE INDEX idx_rubric_criteria_score_tenant ON rubric_criteria_score(tenant_id);
CREATE UNIQUE INDEX uk_moderation_grader_final ON moderation_grader(assignment_id) WHERE is_final_grader = TRUE;
CREATE UNIQUE INDEX uk_provisional_grade_selected ON provisional_grade(submission_id) WHERE is_selected = TRUE;
CREATE INDEX idx_grade_entry_graded_by_fk ON grade_entry(graded_by);
CREATE INDEX idx_grade_item_discussion_id_fk ON grade_item(discussion_id);
CREATE INDEX idx_gradebook_grade_scale_id_fk ON gradebook(grade_scale_id);
CREATE INDEX idx_gradebook_locked_by_fk ON gradebook(locked_by);
CREATE INDEX idx_rubric_criteria_score_level_id_fk ON rubric_criteria_score(level_id);
CREATE INDEX idx_final_grade_submission_section_id_fk ON final_grade_submission(section_id);
CREATE INDEX idx_final_grade_submission_sis_integration_id_fk ON final_grade_submission(sis_integration_id);
CREATE INDEX idx_final_grade_submission_submitted_by_fk ON final_grade_submission(submitted_by);
CREATE INDEX idx_final_grade_submission_tenant_id_fk ON final_grade_submission(tenant_id);
CREATE INDEX idx_grade_item_discussion_checkpoint_id_fk ON grade_item(discussion_checkpoint_id);
CREATE INDEX idx_gradebook_custom_column_course_id_fk ON gradebook_custom_column(course_id);
CREATE INDEX idx_gradebook_custom_column_tenant_id_fk ON gradebook_custom_column(tenant_id);
CREATE INDEX idx_gradebook_custom_column_value_tenant_id_fk ON gradebook_custom_column_value(tenant_id);
CREATE INDEX idx_gradebook_custom_column_value_user_id_fk ON gradebook_custom_column_value(user_id);
CREATE INDEX idx_gradebook_overridden_by_fk ON gradebook(overridden_by);
CREATE INDEX idx_gradebook_period_grade_grading_period_id_fk ON gradebook_period_grade(grading_period_id);
CREATE INDEX idx_gradebook_period_grade_tenant_id_fk ON gradebook_period_grade(tenant_id);
CREATE INDEX idx_grader_assignment_assessment_id_fk ON grader_assignment(assessment_id);
CREATE INDEX idx_grader_assignment_assignment_id_fk ON grader_assignment(assignment_id);
CREATE INDEX idx_grader_assignment_grader_user_id_fk ON grader_assignment(grader_user_id);
CREATE INDEX idx_grader_assignment_group_id_fk ON grader_assignment(group_id);
CREATE INDEX idx_grader_assignment_section_id_fk ON grader_assignment(section_id);
CREATE INDEX idx_grader_assignment_student_user_id_fk ON grader_assignment(student_user_id);
CREATE INDEX idx_grader_assignment_tenant_id_fk ON grader_assignment(tenant_id);
CREATE INDEX idx_grading_period_grading_period_group_id_fk ON grading_period(grading_period_group_id);
CREATE INDEX idx_grading_period_group_tenant_id_fk ON grading_period_group(tenant_id);
CREATE INDEX idx_grading_period_group_term_id_fk ON grading_period_group(term_id);
CREATE INDEX idx_grading_period_tenant_id_fk ON grading_period(tenant_id);
CREATE INDEX idx_late_policy_tenant_id_fk ON late_policy(tenant_id);
CREATE INDEX idx_moderation_grader_grader_user_id_fk ON moderation_grader(grader_user_id);
CREATE INDEX idx_moderation_grader_tenant_id_fk ON moderation_grader(tenant_id);
CREATE INDEX idx_provisional_grade_grader_user_id_fk ON provisional_grade(grader_user_id);
CREATE INDEX idx_provisional_grade_rubric_assessment_id_fk ON provisional_grade(rubric_assessment_id);
CREATE INDEX idx_provisional_grade_selected_by_fk ON provisional_grade(selected_by);
CREATE INDEX idx_provisional_grade_tenant_id_fk ON provisional_grade(tenant_id);
CREATE INDEX idx_rubric_criteria_outcome_id_fk ON rubric_criteria(outcome_id);
CREATE INDEX idx_rubric_template_org_unit_id_fk ON rubric_template(org_unit_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_grade_scale_date_updated BEFORE UPDATE ON grade_scale FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_grade_category_date_updated BEFORE UPDATE ON grade_category FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_grade_item_date_updated BEFORE UPDATE ON grade_item FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_gradebook_date_updated BEFORE UPDATE ON gradebook FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_grade_entry_date_updated BEFORE UPDATE ON grade_entry FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_rubric_template_date_updated BEFORE UPDATE ON rubric_template FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_rubric_criteria_date_updated BEFORE UPDATE ON rubric_criteria FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_rubric_assessment_date_updated BEFORE UPDATE ON rubric_assessment FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_rubric_criteria_score_date_updated BEFORE UPDATE ON rubric_criteria_score FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_late_policy_date_updated BEFORE UPDATE ON late_policy FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_provisional_grade_date_updated BEFORE UPDATE ON provisional_grade FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_grading_period_group_date_updated BEFORE UPDATE ON grading_period_group FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_grading_period_date_updated BEFORE UPDATE ON grading_period FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_gradebook_period_grade_date_updated BEFORE UPDATE ON gradebook_period_grade FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_gradebook_custom_column_date_updated BEFORE UPDATE ON gradebook_custom_column FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_gradebook_custom_column_value_date_updated BEFORE UPDATE ON gradebook_custom_column_value FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_final_grade_submission_date_updated BEFORE UPDATE ON final_grade_submission FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_grade_entry_history AFTER UPDATE ON grade_entry
  FOR EACH ROW EXECUTE FUNCTION grade_entry_history();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE grade_scale ENABLE ROW LEVEL SECURITY;
ALTER TABLE grade_scale FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_grade_scale_tenant ON grade_scale FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE grade_scale_item ENABLE ROW LEVEL SECURITY;
ALTER TABLE grade_scale_item FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_grade_scale_item_tenant ON grade_scale_item FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE grade_category ENABLE ROW LEVEL SECURITY;
ALTER TABLE grade_category FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_grade_category_tenant ON grade_category FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE grade_item ENABLE ROW LEVEL SECURITY;
ALTER TABLE grade_item FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_grade_item_tenant ON grade_item FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE gradebook ENABLE ROW LEVEL SECURITY;
ALTER TABLE gradebook FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_gradebook_tenant ON gradebook FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE grade_entry ENABLE ROW LEVEL SECURITY;
ALTER TABLE grade_entry FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_grade_entry_tenant ON grade_entry FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE grade_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE grade_history FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_grade_history_tenant ON grade_history FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE rubric_template ENABLE ROW LEVEL SECURITY;
ALTER TABLE rubric_template FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_rubric_template_tenant ON rubric_template FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE rubric_criteria ENABLE ROW LEVEL SECURITY;
ALTER TABLE rubric_criteria FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_rubric_criteria_tenant ON rubric_criteria FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE rubric_level ENABLE ROW LEVEL SECURITY;
ALTER TABLE rubric_level FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_rubric_level_tenant ON rubric_level FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE rubric_assessment ENABLE ROW LEVEL SECURITY;
ALTER TABLE rubric_assessment FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_rubric_assessment_tenant ON rubric_assessment FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE rubric_criteria_score ENABLE ROW LEVEL SECURITY;
ALTER TABLE rubric_criteria_score FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_rubric_criteria_score_tenant ON rubric_criteria_score FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE late_policy ENABLE ROW LEVEL SECURITY;
ALTER TABLE late_policy FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_late_policy_tenant ON late_policy FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE moderation_grader ENABLE ROW LEVEL SECURITY;
ALTER TABLE moderation_grader FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_moderation_grader_tenant ON moderation_grader FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE provisional_grade ENABLE ROW LEVEL SECURITY;
ALTER TABLE provisional_grade FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_provisional_grade_tenant ON provisional_grade FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE grader_assignment ENABLE ROW LEVEL SECURITY;
ALTER TABLE grader_assignment FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_grader_assignment_tenant ON grader_assignment FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE grading_period_group ENABLE ROW LEVEL SECURITY;
ALTER TABLE grading_period_group FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_grading_period_group_tenant ON grading_period_group FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE grading_period ENABLE ROW LEVEL SECURITY;
ALTER TABLE grading_period FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_grading_period_tenant ON grading_period FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE gradebook_period_grade ENABLE ROW LEVEL SECURITY;
ALTER TABLE gradebook_period_grade FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_gradebook_period_grade_tenant ON gradebook_period_grade FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE gradebook_custom_column ENABLE ROW LEVEL SECURITY;
ALTER TABLE gradebook_custom_column FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_gradebook_custom_column_tenant ON gradebook_custom_column FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE gradebook_custom_column_value ENABLE ROW LEVEL SECURITY;
ALTER TABLE gradebook_custom_column_value FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_gradebook_custom_column_value_tenant ON gradebook_custom_column_value FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE final_grade_submission ENABLE ROW LEVEL SECURITY;
ALTER TABLE final_grade_submission FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_final_grade_submission_tenant ON final_grade_submission FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
