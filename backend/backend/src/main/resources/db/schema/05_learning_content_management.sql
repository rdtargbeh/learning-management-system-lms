-- ================================================================================
-- 05. LEARNING CONTENT MANAGEMENT (16 tables)
-- ================================================================================
-- Tables: content_type, content, content_block, video_lecture, interactive_content, content_permission, system_resource_library, module_prerequisite, module_completion_requirement, student_module_progress, release_condition, mastery_path_rule, mastery_path_range, mastery_path_set_item, course_pace, course_pace_item
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- ContentType: Types of learning content available
CREATE TABLE content_type (
    type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    type_name VARCHAR(100) NOT NULL,
    type_code VARCHAR(50),
    description TEXT,
    icon_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_content_type_name UNIQUE (type_name, tenant_id),
    CONSTRAINT uk_content_type_code UNIQUE (type_code, tenant_id),
    CONSTRAINT ck_content_type_name CHECK (LENGTH(type_name) >= 3),
    CONSTRAINT ck_content_type_code CHECK (type_code IS NULL OR LENGTH(type_code) >= 2)
);

-- Content: Primary content resource table
CREATE TABLE content (
    content_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    module_id UUID,
    type_id UUID NOT NULL,
    content_title VARCHAR(255) NOT NULL,
    content_description TEXT,
    content_body TEXT,
    content_html JSONB,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    is_front_page BOOLEAN NOT NULL DEFAULT FALSE,
    editing_roles VARCHAR(30) NOT NULL DEFAULT 'TEACHERS',
    display_order INT DEFAULT 0,
    estimated_duration_minutes INT,
    access_level VARCHAR(50) DEFAULT 'ENROLLED',
    date_published TIMESTAMPTZ,
    date_start DATE,
    date_end DATE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_content_title CHECK (LENGTH(content_title) >= 3),
    CONSTRAINT ck_content_access_level CHECK (access_level IN ('ENROLLED', 'RESTRICTED', 'PUBLIC', 'INSTRUCTOR_ONLY')),
    CONSTRAINT ck_content_duration CHECK (estimated_duration_minutes IS NULL OR estimated_duration_minutes > 0),
    CONSTRAINT ck_content_dates CHECK (date_end IS NULL OR date_end >= date_start),
    CONSTRAINT ck_content_editing_roles CHECK (editing_roles IN ('TEACHERS','TEACHERS_STUDENTS','ANYONE'))
);

-- ContentBlock: Building blocks within content (paragraphs, sections, etc.)
CREATE TABLE content_block (
    block_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    content_id UUID NOT NULL,
    block_number INT NOT NULL,
    block_type VARCHAR(50),
    block_title VARCHAR(255),
    block_content TEXT,
    block_content_html JSONB,
    display_order INT DEFAULT 0,
    metadata JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_content_block_number UNIQUE (content_id, block_number),
    CONSTRAINT ck_content_block_type CHECK (block_type IN ('TEXT', 'IMAGE', 'VIDEO', 'AUDIO', 'QUIZ', 'EXERCISE', 'DISCUSSION', 'CODE', 'TABLE', 'LIST', 'EMBEDDED', 'INTERACTIVE'))
);

-- VideoLecture: Video content (lectures, demonstrations)
CREATE TABLE video_lecture (
    video_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    content_id UUID NOT NULL UNIQUE,
    video_url VARCHAR(500) NOT NULL,
    video_provider VARCHAR(50),
    video_duration_seconds INT,
    thumbnail_url VARCHAR(500),
    transcript TEXT,
    transcript_url VARCHAR(500),
    captions_url VARCHAR(500),
    captions_available BOOLEAN DEFAULT FALSE,
    quality_available VARCHAR(100),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_video_lecture_duration CHECK (video_duration_seconds IS NULL OR video_duration_seconds > 0),
    CONSTRAINT ck_video_lecture_provider CHECK (video_provider IS NULL OR video_provider IN ('YOUTUBE', 'VIMEO', 'WISTIA', 'CUSTOM', 'CLOUDFLARE', 'KALTURA'))
);

-- InteractiveContent: Interactive learning activities (simulations, games, etc.)
CREATE TABLE interactive_content (
    interactive_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    content_id UUID NOT NULL UNIQUE,
    interaction_type VARCHAR(50),
    interaction_url VARCHAR(500),
    launch_method VARCHAR(50),
    lti_tool_id VARCHAR(255),
    scorm_package_id VARCHAR(255),
    allows_multiple_attempts BOOLEAN DEFAULT TRUE,
    max_attempts INT,
    time_limit_minutes INT,
    interaction_instructions TEXT,
    success_criteria TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_interactive_type CHECK (interaction_type IN ('SIMULATION', 'GAME', 'DRAG_DROP', 'SORTING', 'MATCHING', 'BRANCHING', 'LAB', 'POLL', 'SURVEY', 'H5P', 'XERTE')),
    CONSTRAINT ck_interactive_launch_method CHECK (launch_method IN ('EMBED', 'NEW_WINDOW', 'LTI', 'SCORM', 'XAPI', 'IFRAME')),
    CONSTRAINT ck_interactive_max_attempts CHECK (max_attempts IS NULL OR max_attempts > 0),
    CONSTRAINT ck_interactive_time_limit CHECK (time_limit_minutes IS NULL OR time_limit_minutes > 0)
);

-- ContentPermission: Fine-grained access control for content
CREATE TABLE content_permission (
    permission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    content_id UUID NOT NULL,
    role_id UUID,
    user_id UUID,
    permission_type VARCHAR(50) NOT NULL,
    is_granted BOOLEAN NOT NULL DEFAULT TRUE,
    date_granted TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_expires TIMESTAMPTZ,
    granted_by UUID,
    CONSTRAINT ck_content_permission_type CHECK (permission_type IN ('VIEW', 'DOWNLOAD', 'PRINT', 'SHARE', 'EDIT', 'COMMENT', 'ANNOTATE', 'DELETE')),
    CONSTRAINT ck_content_permission_either CHECK (role_id IS NOT NULL OR user_id IS NOT NULL),
    CONSTRAINT ck_content_permission_expiry CHECK (date_expires IS NULL OR date_expires > date_granted)
);

-- SystemResourceLibrary: Centralized resource management (reusable across tenants)
CREATE TABLE system_resource_library (
    resource_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    resource_name VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_url VARCHAR(500),
    resource_description TEXT,
    creator_user_id UUID,
    is_public BOOLEAN DEFAULT FALSE,
    is_approved BOOLEAN DEFAULT FALSE,
    date_approved TIMESTAMPTZ,
    approved_by UUID,
    usage_count INT DEFAULT 0,
    rating NUMERIC(2,1),
    download_count INT DEFAULT 0,
    tags JSONB,
    metadata JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_system_resource_library_name CHECK (LENGTH(resource_name) >= 3),
    CONSTRAINT ck_system_resource_library_type CHECK (resource_type IN ('TEMPLATE', 'WIDGET', 'TOOL', 'PLUGIN', 'ASSET', 'LIBRARY', 'COURSE_DESIGN', 'ASSESSMENT_TEMPLATE')),
    CONSTRAINT ck_system_resource_library_rating CHECK (rating IS NULL OR (rating >= 1.0 AND rating <= 5.0)),
    CONSTRAINT ck_system_resource_library_approved CHECK (is_approved = FALSE OR (is_approved = TRUE AND date_approved IS NOT NULL AND approved_by IS NOT NULL))
);

-- MODULE PREREQUISITES & COMPLETION REQUIREMENTS (Canvas Modules / Blackboard release conditions)
CREATE TABLE module_prerequisite (
    module_prerequisite_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    module_id UUID NOT NULL,
    prerequisite_module_id UUID NOT NULL,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_module_prerequisite UNIQUE (module_id, prerequisite_module_id),
    CONSTRAINT ck_module_prerequisite_not_self CHECK (module_id <> prerequisite_module_id)
);

CREATE TABLE module_completion_requirement (
    requirement_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    module_id UUID NOT NULL,
    content_id UUID,
    assignment_id UUID,
    assessment_id UUID,
    discussion_id UUID,
    requirement_type VARCHAR(30) NOT NULL,
    min_score NUMERIC(8,2),
    display_order INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_module_completion_requirement_one_item CHECK (num_nonnulls(content_id, assignment_id, assessment_id, discussion_id) = 1),
    CONSTRAINT ck_module_completion_requirement_type CHECK (requirement_type IN ('VIEW','MARK_DONE','CONTRIBUTE','SUBMIT','MIN_SCORE')),
    CONSTRAINT ck_module_completion_requirement_score CHECK (requirement_type <> 'MIN_SCORE' OR min_score IS NOT NULL)
);

CREATE TABLE student_module_progress (
    module_progress_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    module_id UUID NOT NULL,
    user_id UUID NOT NULL,
    progress_status VARCHAR(20) NOT NULL DEFAULT 'LOCKED',
    requirements_met JSONB,
    date_unlocked TIMESTAMPTZ,
    date_completed TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_student_module_progress UNIQUE (module_id, user_id),
    CONSTRAINT ck_student_module_progress_status CHECK (progress_status IN ('LOCKED','UNLOCKED','STARTED','COMPLETED'))
);

CREATE TABLE release_condition (
    condition_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id UUID NOT NULL,
    condition_type VARCHAR(20) NOT NULL,
    date_from TIMESTAMPTZ,
    date_until TIMESTAMPTZ,
    member_user_id UUID,
    member_group_id UUID,
    member_section_id UUID,
    member_tag_id UUID,
    source_type VARCHAR(20),
    source_id UUID,
    min_score_percent NUMERIC(5,2),
    max_score_percent NUMERIC(5,2),
    condition_group INT NOT NULL DEFAULT 1,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_release_condition_target CHECK (target_type IN ('CONTENT','MODULE','ASSIGNMENT','ASSESSMENT','DISCUSSION','FILE','ANNOUNCEMENT')),
    CONSTRAINT ck_release_condition_type CHECK (condition_type IN ('DATE','MEMBER','PERFORMANCE','SUBMITTED','VIEWED','COMPLETED')),
    CONSTRAINT ck_release_condition_source CHECK (source_type IS NULL OR source_type IN ('CONTENT','ASSIGNMENT','ASSESSMENT','DISCUSSION','GRADE_ITEM','MODULE')),
    CONSTRAINT ck_release_condition_scores CHECK (min_score_percent IS NULL OR max_score_percent IS NULL OR max_score_percent >= min_score_percent),
    CONSTRAINT ck_release_condition_shape CHECK ( (condition_type = 'DATE' AND num_nonnulls(date_from, date_until) >= 1) OR (condition_type = 'MEMBER' AND num_nonnulls(member_user_id, member_group_id, member_section_id, member_tag_id) = 1) OR (condition_type IN ('PERFORMANCE','SUBMITTED','VIEWED','COMPLETED') AND source_id IS NOT NULL AND source_type IS NOT NULL))
);

CREATE TABLE mastery_path_rule (
    rule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    trigger_assignment_id UUID,
    trigger_assessment_id UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_mastery_path_rule_trigger CHECK (num_nonnulls(trigger_assignment_id, trigger_assessment_id) = 1)
);

CREATE TABLE mastery_path_range (
    range_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    rule_id UUID NOT NULL,
    lower_bound_percent NUMERIC(5,2) NOT NULL,
    upper_bound_percent NUMERIC(5,2) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT ck_mastery_path_range_bounds CHECK (lower_bound_percent >= 0 AND upper_bound_percent >= lower_bound_percent)
);

CREATE TABLE mastery_path_set_item (
    set_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    range_id UUID NOT NULL,
    set_number INT NOT NULL DEFAULT 1,
    assignment_id UUID,
    assessment_id UUID,
    discussion_id UUID,
    content_id UUID,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT ck_mastery_path_set_item_one CHECK (num_nonnulls(assignment_id, assessment_id, discussion_id, content_id) = 1)
);

CREATE TABLE course_pace (
    pace_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    section_id UUID,
    user_id UUID,
    pace_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    exclude_weekends BOOLEAN NOT NULL DEFAULT TRUE,
    blackout_dates JSONB,
    date_end TIMESTAMPTZ,
    date_published TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_course_pace_scope CHECK (num_nonnulls(section_id, user_id) <= 1),
    CONSTRAINT ck_course_pace_status CHECK (pace_status IN ('DRAFT','PUBLISHED','ARCHIVED'))
);

CREATE TABLE course_pace_item (
    pace_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    pace_id UUID NOT NULL,
    module_id UUID NOT NULL,
    content_id UUID,
    assignment_id UUID,
    assessment_id UUID,
    discussion_id UUID,
    duration_days INT NOT NULL DEFAULT 1,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT ck_course_pace_item_one CHECK (num_nonnulls(content_id, assignment_id, assessment_id, discussion_id) = 1),
    CONSTRAINT ck_course_pace_item_duration CHECK (duration_days >= 0)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_content_type_tenant ON content_type(tenant_id);
CREATE INDEX idx_content_type_code ON content_type(type_code);
CREATE INDEX idx_content_type_is_active ON content_type(is_active);
CREATE INDEX idx_content_tenant ON content(tenant_id);
CREATE INDEX idx_content_course ON content(course_id);
CREATE INDEX idx_content_module ON content(module_id);
CREATE INDEX idx_content_type ON content(type_id);
CREATE INDEX idx_content_is_published ON content(is_published);
CREATE INDEX idx_content_display_order ON content(course_id, module_id, display_order);
CREATE INDEX idx_content_dates ON content(date_start, date_end);
CREATE INDEX idx_content_block_content ON content_block(content_id);
CREATE INDEX idx_content_block_type ON content_block(block_type);
CREATE INDEX idx_content_block_display_order ON content_block(content_id, display_order);
CREATE INDEX idx_video_lecture_provider ON video_lecture(video_provider);
CREATE INDEX idx_interactive_content_type ON interactive_content(interaction_type);
CREATE INDEX idx_interactive_content_launch ON interactive_content(launch_method);
CREATE INDEX idx_content_permission_content ON content_permission(content_id);
CREATE INDEX idx_content_permission_role ON content_permission(role_id);
CREATE INDEX idx_content_permission_user ON content_permission(user_id);
CREATE INDEX idx_content_permission_type ON content_permission(permission_type);
CREATE INDEX idx_content_permission_expires ON content_permission(date_expires);
CREATE INDEX idx_content_permission_granted ON content_permission(is_granted) WHERE is_granted = TRUE;
CREATE INDEX idx_system_resource_library_tenant ON system_resource_library(tenant_id);
CREATE INDEX idx_system_resource_library_type ON system_resource_library(resource_type);
CREATE INDEX idx_system_resource_library_is_public ON system_resource_library(is_public);
CREATE INDEX idx_system_resource_library_is_approved ON system_resource_library(is_approved);
CREATE INDEX idx_system_resource_library_creator ON system_resource_library(creator_user_id);
CREATE INDEX idx_system_resource_library_rating ON system_resource_library(rating DESC);
CREATE UNIQUE INDEX uk_content_front_page ON content(course_id) WHERE is_front_page = TRUE;
CREATE INDEX idx_content_block_tenant ON content_block(tenant_id);
CREATE INDEX idx_video_lecture_tenant ON video_lecture(tenant_id);
CREATE INDEX idx_interactive_content_tenant ON interactive_content(tenant_id);
CREATE INDEX idx_content_permission_tenant ON content_permission(tenant_id);
CREATE INDEX idx_release_condition_target ON release_condition(target_type, target_id);
CREATE INDEX idx_content_permission_granted_by_fk ON content_permission(granted_by);
CREATE INDEX idx_system_resource_library_approved_by_fk ON system_resource_library(approved_by);
CREATE INDEX idx_module_completion_requirement_assessment_id_fk ON module_completion_requirement(assessment_id);
CREATE INDEX idx_module_completion_requirement_assignment_id_fk ON module_completion_requirement(assignment_id);
CREATE INDEX idx_module_completion_requirement_content_id_fk ON module_completion_requirement(content_id);
CREATE INDEX idx_module_completion_requirement_discussion_id_fk ON module_completion_requirement(discussion_id);
CREATE INDEX idx_module_completion_requirement_module_id_fk ON module_completion_requirement(module_id);
CREATE INDEX idx_module_completion_requirement_tenant_id_fk ON module_completion_requirement(tenant_id);
CREATE INDEX idx_module_prerequisite_prerequisite_module_id_fk ON module_prerequisite(prerequisite_module_id);
CREATE INDEX idx_module_prerequisite_tenant_id_fk ON module_prerequisite(tenant_id);
CREATE INDEX idx_student_module_progress_tenant_id_fk ON student_module_progress(tenant_id);
CREATE INDEX idx_student_module_progress_user_id_fk ON student_module_progress(user_id);
CREATE INDEX idx_course_pace_course_id_fk ON course_pace(course_id);
CREATE INDEX idx_course_pace_item_assessment_id_fk ON course_pace_item(assessment_id);
CREATE INDEX idx_course_pace_item_assignment_id_fk ON course_pace_item(assignment_id);
CREATE INDEX idx_course_pace_item_content_id_fk ON course_pace_item(content_id);
CREATE INDEX idx_course_pace_item_discussion_id_fk ON course_pace_item(discussion_id);
CREATE INDEX idx_course_pace_item_module_id_fk ON course_pace_item(module_id);
CREATE INDEX idx_course_pace_item_pace_id_fk ON course_pace_item(pace_id);
CREATE INDEX idx_course_pace_item_tenant_id_fk ON course_pace_item(tenant_id);
CREATE INDEX idx_course_pace_section_id_fk ON course_pace(section_id);
CREATE INDEX idx_course_pace_tenant_id_fk ON course_pace(tenant_id);
CREATE INDEX idx_course_pace_user_id_fk ON course_pace(user_id);
CREATE INDEX idx_mastery_path_range_rule_id_fk ON mastery_path_range(rule_id);
CREATE INDEX idx_mastery_path_range_tenant_id_fk ON mastery_path_range(tenant_id);
CREATE INDEX idx_mastery_path_rule_course_id_fk ON mastery_path_rule(course_id);
CREATE INDEX idx_mastery_path_rule_tenant_id_fk ON mastery_path_rule(tenant_id);
CREATE INDEX idx_mastery_path_rule_trigger_assessment_id_fk ON mastery_path_rule(trigger_assessment_id);
CREATE INDEX idx_mastery_path_rule_trigger_assignment_id_fk ON mastery_path_rule(trigger_assignment_id);
CREATE INDEX idx_mastery_path_set_item_assessment_id_fk ON mastery_path_set_item(assessment_id);
CREATE INDEX idx_mastery_path_set_item_assignment_id_fk ON mastery_path_set_item(assignment_id);
CREATE INDEX idx_mastery_path_set_item_content_id_fk ON mastery_path_set_item(content_id);
CREATE INDEX idx_mastery_path_set_item_discussion_id_fk ON mastery_path_set_item(discussion_id);
CREATE INDEX idx_mastery_path_set_item_range_id_fk ON mastery_path_set_item(range_id);
CREATE INDEX idx_mastery_path_set_item_tenant_id_fk ON mastery_path_set_item(tenant_id);
CREATE INDEX idx_release_condition_course_id_fk ON release_condition(course_id);
CREATE INDEX idx_release_condition_member_group_id_fk ON release_condition(member_group_id);
CREATE INDEX idx_release_condition_member_section_id_fk ON release_condition(member_section_id);
CREATE INDEX idx_release_condition_member_tag_id_fk ON release_condition(member_tag_id);
CREATE INDEX idx_release_condition_member_user_id_fk ON release_condition(member_user_id);
CREATE INDEX idx_release_condition_tenant_id_fk ON release_condition(tenant_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_content_date_updated BEFORE UPDATE ON content FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_content_block_date_updated BEFORE UPDATE ON content_block FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_video_lecture_date_updated BEFORE UPDATE ON video_lecture FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_interactive_content_date_updated BEFORE UPDATE ON interactive_content FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_system_resource_library_date_updated BEFORE UPDATE ON system_resource_library FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_student_module_progress_date_updated BEFORE UPDATE ON student_module_progress FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_mastery_path_rule_date_updated BEFORE UPDATE ON mastery_path_rule FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_course_pace_date_updated BEFORE UPDATE ON course_pace FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE content_type ENABLE ROW LEVEL SECURITY;
ALTER TABLE content_type FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_content_type_tenant ON content_type FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE content ENABLE ROW LEVEL SECURITY;
ALTER TABLE content FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_content_tenant ON content FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE content_block ENABLE ROW LEVEL SECURITY;
ALTER TABLE content_block FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_content_block_tenant ON content_block FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE video_lecture ENABLE ROW LEVEL SECURITY;
ALTER TABLE video_lecture FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_video_lecture_tenant ON video_lecture FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE interactive_content ENABLE ROW LEVEL SECURITY;
ALTER TABLE interactive_content FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_interactive_content_tenant ON interactive_content FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE content_permission ENABLE ROW LEVEL SECURITY;
ALTER TABLE content_permission FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_content_permission_tenant ON content_permission FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE system_resource_library ENABLE ROW LEVEL SECURITY;
ALTER TABLE system_resource_library FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_system_resource_library_read ON system_resource_library FOR SELECT USING (tenant_id IS NULL OR tenant_id = app_current_tenant_id() OR app_is_platform_admin());
CREATE POLICY rls_system_resource_library_write ON system_resource_library FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE module_prerequisite ENABLE ROW LEVEL SECURITY;
ALTER TABLE module_prerequisite FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_module_prerequisite_tenant ON module_prerequisite FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE module_completion_requirement ENABLE ROW LEVEL SECURITY;
ALTER TABLE module_completion_requirement FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_module_completion_requirement_tenant ON module_completion_requirement FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE student_module_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_module_progress FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_student_module_progress_tenant ON student_module_progress FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE release_condition ENABLE ROW LEVEL SECURITY;
ALTER TABLE release_condition FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_release_condition_tenant ON release_condition FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE mastery_path_rule ENABLE ROW LEVEL SECURITY;
ALTER TABLE mastery_path_rule FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_mastery_path_rule_tenant ON mastery_path_rule FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE mastery_path_range ENABLE ROW LEVEL SECURITY;
ALTER TABLE mastery_path_range FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_mastery_path_range_tenant ON mastery_path_range FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE mastery_path_set_item ENABLE ROW LEVEL SECURITY;
ALTER TABLE mastery_path_set_item FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_mastery_path_set_item_tenant ON mastery_path_set_item FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course_pace ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_pace FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_pace_tenant ON course_pace FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course_pace_item ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_pace_item FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_pace_item_tenant ON course_pace_item FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
