-- ================================================================================
-- 09. ASSIGNMENTS & COURSEWORK (10 tables)
-- ================================================================================
-- Tables: assignment, assignment_submission, assignment_group, assignment_peer_review, feedback, feedback_tag, assignment_override, plagiarism_scan, submission_annotation, comment_library_item
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- Assignment: Assignment definitions
CREATE TABLE assignment (
    assignment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    module_id UUID,
    assignment_title VARCHAR(255) NOT NULL,
    assignment_description TEXT,
    assignment_instructions TEXT,
    assignment_type VARCHAR(50) NOT NULL,
    assignment_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT DEFAULT 0,
    points_possible NUMERIC(8,2),
    omit_from_final_grade BOOLEAN NOT NULL DEFAULT FALSE,
    grading_type VARCHAR(50),
    rubric_template_id UUID,
    is_anonymous_grading BOOLEAN NOT NULL DEFAULT FALSE,
    is_moderated_grading BOOLEAN NOT NULL DEFAULT FALSE,
    final_grader_id UUID,
    grader_count INT,
    graders_anonymous_to_graders BOOLEAN NOT NULL DEFAULT FALSE,
    allow_submissions BOOLEAN DEFAULT TRUE,
    submission_type VARCHAR(50) NOT NULL,
    allowed_submission_types TEXT[],
    allowed_file_extensions TEXT[],
    annotatable_file_id UUID,
    external_tool_id UUID,
    max_submissions INT,
    late_submission_allowed BOOLEAN DEFAULT FALSE,
    late_submission_penalty_percent NUMERIC(5,2),
    date_available DATE,
    date_due DATE,
    date_close DATE,
    allow_group_submission BOOLEAN DEFAULT FALSE,
    group_set_id UUID,
    grade_group_individually BOOLEAN NOT NULL DEFAULT FALSE,
    group_size INT,
    peer_review_required BOOLEAN DEFAULT FALSE,
    peer_review_count INT,
    plagiarism_detection_enabled BOOLEAN DEFAULT FALSE,
    plagiarism_detection_tool VARCHAR(50),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_assignment_title CHECK (LENGTH(assignment_title) >= 3),
    CONSTRAINT ck_assignment_status CHECK (assignment_status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED', 'CLOSED')),
    CONSTRAINT ck_assignment_submission_type CHECK (submission_type IN ('ONLINE', 'OFFLINE', 'HYBRID', 'ON_PAPER')),
    CONSTRAINT ck_assignment_grading_type CHECK (grading_type IS NULL OR grading_type IN ('POINTS', 'PERCENTAGE', 'LETTER_GRADE', 'GPA_SCALE', 'PASS_FAIL', 'COMPLETE_INCOMPLETE')),
    CONSTRAINT ck_assignment_points CHECK (points_possible IS NULL OR points_possible > 0),
    CONSTRAINT ck_assignment_late_penalty CHECK (late_submission_penalty_percent IS NULL OR (late_submission_penalty_percent >= 0 AND late_submission_penalty_percent <= 100)),
    CONSTRAINT ck_assignment_dates CHECK (date_close IS NULL OR date_close >= date_due),
    CONSTRAINT ck_assignment_group_size CHECK (group_size IS NULL OR group_size > 1),
    CONSTRAINT ck_assignment_peer_review CHECK (peer_review_count IS NULL OR peer_review_count > 0),
    CONSTRAINT ck_assignment_type CHECK (assignment_type IN ('ESSAY','DISCUSSION','QUIZ','FILE_UPLOAD','TEXT_ENTRY','URL_SUBMISSION','MEDIA_RECORDING','CODE_SUBMISSION','STUDENT_ANNOTATION','EXTERNAL_TOOL','ON_PAPER','NO_SUBMISSION')),
    CONSTRAINT ck_assignment_grader_count CHECK (grader_count IS NULL OR grader_count BETWEEN 1 AND 10)
);

-- AssignmentSubmission: Student submissions to assignments
CREATE TABLE assignment_submission (
    submission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assignment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    submission_number INT NOT NULL,
    submission_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    submitted_at TIMESTAMPTZ,
    submitted_late BOOLEAN DEFAULT FALSE,
    late_policy_status VARCHAR(20) NOT NULL DEFAULT 'NONE',
    seconds_late_override BIGINT,
    is_excused BOOLEAN NOT NULL DEFAULT FALSE,
    submission_type_used VARCHAR(30),
    is_group_submission BOOLEAN DEFAULT FALSE,
    group_id UUID,
    submission_text TEXT,
    submission_url VARCHAR(500),
    submission_notes TEXT,
    word_count INT,
    character_count INT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_assignment_submission UNIQUE (assignment_id, user_id, submission_number),
    CONSTRAINT ck_assignment_submission_status CHECK (submission_status IN ('DRAFT', 'SUBMITTED', 'GRADED', 'RETURNED', 'REASSIGNED')),
    CONSTRAINT ck_assignment_submission_number CHECK (submission_number > 0),
    CONSTRAINT ck_assignment_submission_word_count CHECK (word_count IS NULL OR word_count >= 0),
    CONSTRAINT ck_assignment_submission_char_count CHECK (character_count IS NULL OR character_count >= 0),
    CONSTRAINT ck_assignment_submission_late_status CHECK (late_policy_status IN ('NONE','LATE','MISSING','EXTENDED'))
);

-- AssignmentGroup: Groups for group assignments
CREATE TABLE assignment_group (
    group_assignment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assignment_id UUID NOT NULL,
    group_id UUID NOT NULL,
    group_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    date_assigned DATE NOT NULL,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_assignment_group UNIQUE (assignment_id, group_id),
    CONSTRAINT ck_assignment_group_status CHECK (group_status IN ('ACTIVE', 'INACTIVE', 'COMPLETED', 'ARCHIVED'))
);

-- AssignmentPeerReview: Peer review assignments and reviews
CREATE TABLE assignment_peer_review (
    peer_review_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assignment_id UUID NOT NULL,
    reviewer_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    review_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    review_text TEXT,
    review_rating INT,
    plagiarism_score NUMERIC(5,2),
    plagiarism_detected BOOLEAN DEFAULT FALSE,
    date_assigned TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_submitted TIMESTAMPTZ,
    anonymous_review BOOLEAN DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_assignment_peer_review UNIQUE (reviewer_id, submission_id),
    CONSTRAINT ck_assignment_peer_review_status CHECK (review_status IN ('PENDING', 'IN_PROGRESS', 'SUBMITTED', 'GRADED', 'DISMISSED')),
    CONSTRAINT ck_assignment_peer_review_rating CHECK (review_rating IS NULL OR (review_rating >= 1 AND review_rating <= 5)),
    CONSTRAINT ck_assignment_peer_review_plagiarism CHECK (plagiarism_score IS NULL OR (plagiarism_score >= 0 AND plagiarism_score <= 100))
);

-- Feedback: Instructor feedback on assignments
CREATE TABLE feedback (
    feedback_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    submission_id UUID NOT NULL,
    evaluator_id UUID NOT NULL,
    grade_earned NUMERIC(8,2),
    grade_percentage NUMERIC(5,2),
    grade_letter VARCHAR(5),
    feedback_text TEXT,
    feedback_rubric_data JSONB,
    is_passed BOOLEAN,
    date_graded TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    grading_rubric_id UUID,
    grading_time_minutes INT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_feedback_grade CHECK (grade_earned IS NULL OR grade_earned >= 0),
    CONSTRAINT ck_feedback_percentage CHECK (grade_percentage IS NULL OR (grade_percentage >= 0 AND grade_percentage <= 100)),
    CONSTRAINT ck_feedback_grading_time CHECK (grading_time_minutes IS NULL OR grading_time_minutes > 0)
);

-- FeedbackTag: Tags for feedback categorization (e.g., grammar, structure, clarity)
CREATE TABLE feedback_tag (
    tag_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    feedback_id UUID NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    tag_category VARCHAR(50),
    tag_position_start INT,
    tag_position_end INT,
    tag_severity VARCHAR(50),
    suggestion TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_feedback_tag_name CHECK (LENGTH(tag_name) >= 2),
    CONSTRAINT ck_feedback_tag_category CHECK (tag_category IN ('GRAMMAR', 'SPELLING', 'PUNCTUATION', 'STYLE', 'STRUCTURE', 'CLARITY', 'LOGIC', 'CITATION', 'CONTENT', 'FORMATTING', 'OTHER')),
    CONSTRAINT ck_feedback_tag_severity CHECK (tag_severity IN ('CRITICAL', 'MAJOR', 'MINOR', 'SUGGESTION')),
    CONSTRAINT ck_feedback_tag_position CHECK (tag_position_end IS NULL OR tag_position_end > tag_position_start)
);

-- ASSIGNMENT OVERRIDE: per-student / section / group dates & attempts ("assign to") for assignments OR assessments
CREATE TABLE assignment_override (
    override_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    assignment_id UUID,
    assessment_id UUID,
    target_type VARCHAR(20) NOT NULL,
    target_user_id UUID,
    target_section_id UUID,
    target_group_id UUID,
    target_tag_id UUID,
    discussion_checkpoint_id UUID,
    date_available TIMESTAMPTZ,
    date_due TIMESTAMPTZ,
    date_close TIMESTAMPTZ,
    max_attempts_override INT,
    time_limit_minutes_override INT,
    reason TEXT,
    created_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_assignment_override_one_parent CHECK (num_nonnulls(assignment_id, assessment_id, discussion_checkpoint_id) = 1),
    CONSTRAINT ck_assignment_override_target CHECK ( (target_type = 'STUDENT' AND target_user_id IS NOT NULL AND num_nonnulls(target_section_id, target_group_id, target_tag_id) = 0) OR (target_type = 'SECTION' AND target_section_id IS NOT NULL AND num_nonnulls(target_user_id, target_group_id, target_tag_id) = 0) OR (target_type = 'GROUP' AND target_group_id IS NOT NULL AND num_nonnulls(target_user_id, target_section_id, target_tag_id) = 0) OR (target_type = 'TAG' AND target_tag_id IS NOT NULL AND num_nonnulls(target_user_id, target_section_id, target_group_id) = 0)),
    CONSTRAINT ck_assignment_override_dates CHECK (date_due IS NULL OR date_available IS NULL OR date_due >= date_available)
);

-- PLAGIARISM / ORIGINALITY SCAN (Turnitin, Copyleaks, etc.)
CREATE TABLE plagiarism_scan (
    scan_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    submission_id UUID NOT NULL,
    file_id UUID,
    provider VARCHAR(50) NOT NULL,
    provider_scan_ref VARCHAR(255),
    scan_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    similarity_score NUMERIC(5,2),
    ai_writing_score NUMERIC(5,2),
    report_url VARCHAR(1000),
    matched_sources JSONB,
    date_submitted_to_provider TIMESTAMPTZ,
    date_completed TIMESTAMPTZ,
    error_message TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_plagiarism_scan_status CHECK (scan_status IN ('PENDING','SUBMITTED','COMPLETED','FAILED','CANCELLED')),
    CONSTRAINT ck_plagiarism_scan_score CHECK (similarity_score IS NULL OR similarity_score BETWEEN 0 AND 100),
    CONSTRAINT ck_plagiarism_scan_ai_score CHECK (ai_writing_score IS NULL OR ai_writing_score BETWEEN 0 AND 100)
);

CREATE TABLE submission_annotation (
    annotation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    submission_id UUID NOT NULL,
    file_id UUID,
    author_user_id UUID NOT NULL,
    annotation_type VARCHAR(20) NOT NULL,
    page_number INT,
    coordinates JSONB,
    annotation_text TEXT,
    color VARCHAR(20),
    parent_annotation_id UUID,
    is_draft BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_submission_annotation_type CHECK (annotation_type IN ('HIGHLIGHT','STRIKEOUT','POINT','AREA','TEXT','FREE_DRAW','COMMENT','STAMP')),
    CONSTRAINT ck_submission_annotation_page CHECK (page_number IS NULL OR page_number > 0)
);

CREATE TABLE comment_library_item (
    library_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    comment_text TEXT NOT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_assignment_tenant ON assignment(tenant_id);
CREATE INDEX idx_assignment_course ON assignment(course_id);
CREATE INDEX idx_assignment_module ON assignment(module_id);
CREATE INDEX idx_assignment_is_published ON assignment(is_published);
CREATE INDEX idx_assignment_status ON assignment(assignment_status);
CREATE INDEX idx_assignment_due_date ON assignment(date_due);
CREATE INDEX idx_assignment_type ON assignment(assignment_type);
CREATE INDEX idx_assignment_display_order ON assignment(course_id, module_id, display_order);
CREATE INDEX idx_assignment_submission_assignment ON assignment_submission(assignment_id);
CREATE INDEX idx_assignment_submission_user ON assignment_submission(user_id);
CREATE INDEX idx_assignment_submission_status ON assignment_submission(submission_status);
CREATE INDEX idx_assignment_submission_submitted ON assignment_submission(submitted_at DESC);
CREATE INDEX idx_assignment_submission_group ON assignment_submission(group_id);
CREATE INDEX idx_assignment_submission_late ON assignment_submission(submitted_late) WHERE submitted_late = TRUE;
CREATE INDEX idx_assignment_group_assignment ON assignment_group(assignment_id);
CREATE INDEX idx_assignment_group_group ON assignment_group(group_id);
CREATE INDEX idx_assignment_group_status ON assignment_group(group_status);
CREATE INDEX idx_assignment_peer_review_assignment ON assignment_peer_review(assignment_id);
CREATE INDEX idx_assignment_peer_review_reviewer ON assignment_peer_review(reviewer_id);
CREATE INDEX idx_assignment_peer_review_submission ON assignment_peer_review(submission_id);
CREATE INDEX idx_assignment_peer_review_status ON assignment_peer_review(review_status);
CREATE INDEX idx_assignment_peer_review_plagiarism ON assignment_peer_review(plagiarism_detected) WHERE plagiarism_detected = TRUE;
CREATE INDEX idx_feedback_submission ON feedback(submission_id);
CREATE INDEX idx_feedback_evaluator ON feedback(evaluator_id);
CREATE INDEX idx_feedback_date_graded ON feedback(date_graded DESC);
CREATE INDEX idx_feedback_tag_feedback ON feedback_tag(feedback_id);
CREATE INDEX idx_feedback_tag_category ON feedback_tag(tag_category);
CREATE INDEX idx_feedback_tag_severity ON feedback_tag(tag_severity);
CREATE INDEX idx_assignment_submission_tenant ON assignment_submission(tenant_id);
CREATE INDEX idx_assignment_group_tenant ON assignment_group(tenant_id);
CREATE INDEX idx_assignment_peer_review_tenant ON assignment_peer_review(tenant_id);
CREATE INDEX idx_feedback_tenant ON feedback(tenant_id);
CREATE INDEX idx_feedback_tag_tenant ON feedback_tag(tenant_id);
CREATE INDEX idx_assignment_override_assignment ON assignment_override(assignment_id);
CREATE INDEX idx_assignment_override_assessment ON assignment_override(assessment_id);
CREATE INDEX idx_assignment_override_user ON assignment_override(target_user_id);
CREATE INDEX idx_plagiarism_scan_submission ON plagiarism_scan(submission_id);
CREATE INDEX idx_submission_annotation_submission ON submission_annotation(submission_id);
CREATE INDEX idx_assignment_group_set_id_fk ON assignment(group_set_id);
CREATE INDEX idx_assignment_override_created_by_fk ON assignment_override(created_by);
CREATE INDEX idx_assignment_override_target_group_id_fk ON assignment_override(target_group_id);
CREATE INDEX idx_assignment_override_target_section_id_fk ON assignment_override(target_section_id);
CREATE INDEX idx_assignment_override_tenant_id_fk ON assignment_override(tenant_id);
CREATE INDEX idx_assignment_rubric_template_id_fk ON assignment(rubric_template_id);
CREATE INDEX idx_plagiarism_scan_file_id_fk ON plagiarism_scan(file_id);
CREATE INDEX idx_plagiarism_scan_tenant_id_fk ON plagiarism_scan(tenant_id);
CREATE INDEX idx_assignment_annotatable_file_id_fk ON assignment(annotatable_file_id);
CREATE INDEX idx_assignment_external_tool_id_fk ON assignment(external_tool_id);
CREATE INDEX idx_assignment_final_grader_id_fk ON assignment(final_grader_id);
CREATE INDEX idx_assignment_override_discussion_checkpoint_id_fk ON assignment_override(discussion_checkpoint_id);
CREATE INDEX idx_assignment_override_target_tag_id_fk ON assignment_override(target_tag_id);
CREATE INDEX idx_comment_library_item_tenant_id_fk ON comment_library_item(tenant_id);
CREATE INDEX idx_comment_library_item_user_id_fk ON comment_library_item(user_id);
CREATE INDEX idx_submission_annotation_author_user_id_fk ON submission_annotation(author_user_id);
CREATE INDEX idx_submission_annotation_file_id_fk ON submission_annotation(file_id);
CREATE INDEX idx_submission_annotation_parent_annotation_id_fk ON submission_annotation(parent_annotation_id);
CREATE INDEX idx_submission_annotation_tenant_id_fk ON submission_annotation(tenant_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_assignment_date_updated BEFORE UPDATE ON assignment FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_assignment_submission_date_updated BEFORE UPDATE ON assignment_submission FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_assignment_peer_review_date_updated BEFORE UPDATE ON assignment_peer_review FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_feedback_date_updated BEFORE UPDATE ON feedback FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_assignment_override_date_updated BEFORE UPDATE ON assignment_override FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_plagiarism_scan_date_updated BEFORE UPDATE ON plagiarism_scan FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_submission_annotation_date_updated BEFORE UPDATE ON submission_annotation FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_comment_library_item_date_updated BEFORE UPDATE ON comment_library_item FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE assignment ENABLE ROW LEVEL SECURITY;
ALTER TABLE assignment FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_assignment_tenant ON assignment FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE assignment_submission ENABLE ROW LEVEL SECURITY;
ALTER TABLE assignment_submission FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_assignment_submission_tenant ON assignment_submission FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE assignment_group ENABLE ROW LEVEL SECURITY;
ALTER TABLE assignment_group FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_assignment_group_tenant ON assignment_group FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE assignment_peer_review ENABLE ROW LEVEL SECURITY;
ALTER TABLE assignment_peer_review FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_assignment_peer_review_tenant ON assignment_peer_review FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE feedback ENABLE ROW LEVEL SECURITY;
ALTER TABLE feedback FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_feedback_tenant ON feedback FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE feedback_tag ENABLE ROW LEVEL SECURITY;
ALTER TABLE feedback_tag FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_feedback_tag_tenant ON feedback_tag FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE assignment_override ENABLE ROW LEVEL SECURITY;
ALTER TABLE assignment_override FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_assignment_override_tenant ON assignment_override FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE plagiarism_scan ENABLE ROW LEVEL SECURITY;
ALTER TABLE plagiarism_scan FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_plagiarism_scan_tenant ON plagiarism_scan FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE submission_annotation ENABLE ROW LEVEL SECURITY;
ALTER TABLE submission_annotation FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_submission_annotation_tenant ON submission_annotation FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE comment_library_item ENABLE ROW LEVEL SECURITY;
ALTER TABLE comment_library_item FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_comment_library_item_tenant ON comment_library_item FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
