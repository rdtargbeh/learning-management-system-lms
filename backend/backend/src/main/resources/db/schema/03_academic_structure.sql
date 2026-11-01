-- ================================================================================
-- 03. ACADEMIC STRUCTURE (14 tables)
-- ================================================================================
-- Tables: course_category, course, course_section, course_module, course_instructor, course_prerequisite, course_schedule, learning_outcome, org_unit, academic_term, blueprint_association, blueprint_content_lock, blueprint_sync_run, course_navigation_item
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- CourseCategory: Hierarchical course categorization
CREATE TABLE course_category (
    category_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    parent_category_id UUID,
    category_name VARCHAR(100) NOT NULL,
    category_code VARCHAR(50),
    description TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_course_category_name CHECK (LENGTH(category_name) >= 3),
    CONSTRAINT ck_course_category_no_self_parent CHECK (parent_category_id IS NULL OR parent_category_id != category_id)
);

-- Course: Course definitions
CREATE TABLE course (
    course_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    category_id UUID,
    org_unit_id UUID,
    term_id UUID,
    course_code VARCHAR(50) NOT NULL,
    sis_source_id VARCHAR(255),
    course_title VARCHAR(255) NOT NULL,
    course_description TEXT,
    syllabus_html TEXT,
    credit_hours NUMERIC(3,1),
    course_level VARCHAR(50),
    course_type VARCHAR(50),
    total_seats INT,
    course_status VARCHAR(20) NOT NULL DEFAULT 'UNPUBLISHED',
    course_kind VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    date_start TIMESTAMPTZ,
    date_end TIMESTAMPTZ,
    time_zone VARCHAR(50),
    home_page_type VARCHAR(20) NOT NULL DEFAULT 'MODULES',
    course_image_url VARCHAR(1000),
    allow_self_enrollment BOOLEAN NOT NULL DEFAULT FALSE,
    self_enrollment_code VARCHAR(50),
    is_public_syllabus BOOLEAN NOT NULL DEFAULT FALSE,
    content_license VARCHAR(50),
    grading_scale_id UUID,
    grade_calculation_method VARCHAR(20) NOT NULL DEFAULT 'WEIGHTED_CATEGORIES',
    final_grade_override_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    default_post_policy VARCHAR(10) NOT NULL DEFAULT 'AUTOMATIC',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_code_tenant UNIQUE (course_code, tenant_id),
    CONSTRAINT ck_course_code CHECK (LENGTH(course_code) >= 3),
    CONSTRAINT ck_course_title CHECK (LENGTH(course_title) >= 3),
    CONSTRAINT ck_course_credit_hours CHECK (credit_hours IS NULL OR credit_hours > 0),
    CONSTRAINT ck_course_level CHECK (course_level IS NULL OR course_level IN ('INTRODUCTORY', 'INTERMEDIATE', 'ADVANCED', 'GRADUATE')),
    CONSTRAINT ck_course_type CHECK (course_type IS NULL OR course_type IN ('LECTURE', 'LAB', 'SEMINAR', 'ONLINE', 'HYBRID', 'PRACTICUM', 'WORKSHOP')),
    CONSTRAINT ck_course_seats CHECK (total_seats IS NULL OR total_seats > 0),
    CONSTRAINT ck_course_status CHECK (course_status IN ('UNPUBLISHED','PUBLISHED','CONCLUDED','ARCHIVED')),
    CONSTRAINT ck_course_kind CHECK (course_kind IN ('STANDARD','BLUEPRINT','TEMPLATE','ORGANIZATION','SANDBOX')),
    CONSTRAINT ck_course_dates CHECK (date_end IS NULL OR date_start IS NULL OR date_end >= date_start),
    CONSTRAINT ck_course_home_page CHECK (home_page_type IN ('MODULES','PAGE','SYLLABUS','ASSIGNMENTS','ACTIVITY_STREAM')),
    CONSTRAINT ck_course_grade_calc CHECK (grade_calculation_method IN ('WEIGHTED_CATEGORIES','TOTAL_POINTS','CUSTOM_FORMULA')),
    CONSTRAINT ck_course_post_policy CHECK (default_post_policy IN ('AUTOMATIC','MANUAL'))
);

-- CourseSection: Term-specific offering of a course
CREATE TABLE course_section (
    section_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    crosslisted_from_course_id UUID,
    term_id UUID,
    section_number VARCHAR(10),
    sis_source_id VARCHAR(255),
    capacity INT,
    delivery_mode VARCHAR(20) DEFAULT 'IN_PERSON',
    enrolled_count INT DEFAULT 0,
    waitlist_count INT DEFAULT 0,
    date_start DATE,
    date_end DATE,
    registration_open_date DATE,
    registration_close_date DATE,
    is_open_for_enrollment BOOLEAN DEFAULT TRUE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_course_section_capacity CHECK (capacity IS NULL OR capacity > 0),
    CONSTRAINT ck_course_section_enrolled CHECK (enrolled_count >= 0),
    CONSTRAINT ck_course_section_waitlist CHECK (waitlist_count >= 0),
    CONSTRAINT ck_course_section_capacity_integrity CHECK (capacity IS NULL OR enrolled_count <= capacity),
    CONSTRAINT ck_course_section_dates CHECK (date_end IS NULL OR date_end >= date_start),
    CONSTRAINT ck_course_section_reg_dates CHECK (registration_close_date IS NULL OR registration_close_date >= registration_open_date),
    CONSTRAINT ck_course_section_status CHECK (status IN ('ACTIVE','CANCELLED','COMPLETED','PENDING')),
    CONSTRAINT uk_course_section_term UNIQUE (course_id, term_id, section_number),
    CONSTRAINT ck_course_section_delivery CHECK (delivery_mode IS NULL OR delivery_mode IN ('IN_PERSON','ONLINE','HYBRID','HYFLEX'))
);

-- CourseModule: Course content structure inside a section
CREATE TABLE course_module (
    module_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    section_id UUID,
    module_number INT NOT NULL,
    module_title VARCHAR(255) NOT NULL,
    module_description TEXT,
    module_type VARCHAR(50),
    display_order INT DEFAULT 0,
    date_start DATE,
    date_end DATE,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    require_sequential_progress BOOLEAN NOT NULL DEFAULT FALSE,
    date_unlock TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_module_number UNIQUE (section_id,module_number),
    CONSTRAINT ck_course_module_title CHECK (LENGTH(module_title) >= 3),
    CONSTRAINT ck_course_module_type CHECK (module_type IS NULL OR module_type IN ('UNIT','WEEK','TOPIC','SECTION','MODULE')),
    CONSTRAINT ck_course_module_dates CHECK (date_end IS NULL OR date_end >= date_start)
);

-- CourseInstructor: Instructors assigned to courses
CREATE TABLE course_instructor (
    course_instructor_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    section_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_primary_instructor BOOLEAN NOT NULL DEFAULT FALSE,
    date_assigned DATE NOT NULL,
    date_start DATE,
    date_end DATE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_instructor_assignment UNIQUE (section_id,user_id),
    CONSTRAINT ck_course_instructor_role CHECK (role IN ('PRIMARY','CO_INSTRUCTOR','TEACHING_ASSISTANT','GRADER','REVIEWER')),
    CONSTRAINT ck_course_instructor_dates CHECK (date_end IS NULL OR date_end >= date_start)
);

-- CoursePrerequisite: Course prerequisite requirements
CREATE TABLE course_prerequisite (
    prerequisite_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    prerequisite_course_id UUID NOT NULL,
    prerequisite_type VARCHAR(50),
    min_grade VARCHAR(2),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_prerequisite UNIQUE (course_id, prerequisite_course_id),
    CONSTRAINT ck_course_prerequisite_type CHECK (prerequisite_type IN ('REQUIRED', 'RECOMMENDED', 'COREQUISITE')),
    CONSTRAINT ck_course_prerequisite_no_self CHECK (course_id != prerequisite_course_id)
);

-- CourseSchedule: Meeting times and locations for a section
CREATE TABLE course_schedule (
    schedule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    section_id UUID NOT NULL,
    instructor_assignment_id UUID,
    day_of_week VARCHAR(10),
    time_start TIME,
    time_end TIME,
    location VARCHAR(255),
    meeting_type VARCHAR(50),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_course_schedule_day CHECK (day_of_week IS NULL OR day_of_week IN ('MON','TUE','WED','THU','FRI','SAT','SUN')),
    CONSTRAINT ck_course_schedule_time CHECK (time_end IS NULL OR time_end > time_start),
    CONSTRAINT ck_course_schedule_meeting_type CHECK (meeting_type IN ('IN_PERSON','ONLINE','HYBRID','ASYNCHRONOUS','SYNCHRONOUS'))
);

-- LearningOutcome: Intended outcomes for a course
CREATE TABLE learning_outcome (
    outcome_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID,
    org_unit_id UUID,
    outcome_number INT NOT NULL,
    outcome_description TEXT NOT NULL,
    bloom_level VARCHAR(50),
    mastery_points NUMERIC(8,2),
    calculation_method VARCHAR(20) NOT NULL DEFAULT 'DECAYING_AVERAGE',
    calculation_int INT,
    outcome_type VARCHAR(50),
    is_primary BOOLEAN DEFAULT FALSE,
    assessment_method VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_learning_outcome_number UNIQUE (course_id,outcome_number),
    CONSTRAINT ck_learning_outcome_description CHECK (LENGTH(outcome_description) >= 10),
    CONSTRAINT ck_learning_outcome_bloom CHECK (bloom_level IS NULL OR bloom_level IN ('REMEMBER','UNDERSTAND','APPLY','ANALYZE','EVALUATE','CREATE')),
    CONSTRAINT ck_learning_outcome_type CHECK (outcome_type IN ('COGNITIVE','AFFECTIVE','PSYCHOMOTOR','TECHNICAL','GENERAL')),
    CONSTRAINT ck_learning_outcome_owner CHECK (num_nonnulls(course_id, org_unit_id) >= 1),
    CONSTRAINT ck_learning_outcome_calc CHECK (calculation_method IN ('DECAYING_AVERAGE','WEIGHTED_AVERAGE','N_MASTERY','LATEST','HIGHEST','AVERAGE'))
);

-- ORGANIZATION: hierarchical org units (Canvas sub-accounts / Blackboard nodes)
CREATE TABLE org_unit (
    org_unit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    parent_org_unit_id UUID,
    unit_name VARCHAR(255) NOT NULL,
    unit_code VARCHAR(50),
    unit_type VARCHAR(50) NOT NULL DEFAULT 'DEPARTMENT',
    sis_source_id VARCHAR(255),
    branding JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    metadata JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_org_unit_code UNIQUE (tenant_id, unit_code),
    CONSTRAINT ck_org_unit_type CHECK (unit_type IN ('INSTITUTION','CAMPUS','COLLEGE','SCHOOL','DEPARTMENT','PROGRAM','OTHER')),
    CONSTRAINT ck_org_unit_not_self CHECK (parent_org_unit_id IS NULL OR parent_org_unit_id <> org_unit_id)
);

-- ACADEMIC TERM: semester / quarter / session (Canvas enrollment terms)
CREATE TABLE academic_term (
    term_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    calendar_id UUID,
    term_name VARCHAR(100) NOT NULL,
    term_code VARCHAR(50) NOT NULL,
    term_type VARCHAR(50) NOT NULL DEFAULT 'SEMESTER',
    date_start DATE NOT NULL,
    date_end DATE NOT NULL,
    date_census DATE,
    date_add_drop_deadline DATE,
    date_withdraw_deadline DATE,
    date_grades_due DATE,
    sis_source_id VARCHAR(255),
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_academic_term_code UNIQUE (tenant_id, term_code),
    CONSTRAINT ck_academic_term_type CHECK (term_type IN ('SEMESTER','QUARTER','TRIMESTER','SUMMER','WINTER','INTERSESSION','SELF_PACED','OTHER')),
    CONSTRAINT ck_academic_term_dates CHECK (date_end >= date_start)
);

CREATE TABLE blueprint_association (
    blueprint_association_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    blueprint_course_id UUID NOT NULL,
    associated_course_id UUID NOT NULL,
    date_last_synced TIMESTAMPTZ,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_blueprint_association UNIQUE (blueprint_course_id, associated_course_id),
    CONSTRAINT ck_blueprint_association_not_self CHECK (blueprint_course_id <> associated_course_id)
);

CREATE TABLE blueprint_content_lock (
    lock_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    blueprint_course_id UUID NOT NULL,
    object_type VARCHAR(30) NOT NULL,
    object_id UUID NOT NULL,
    lock_content BOOLEAN NOT NULL DEFAULT TRUE,
    lock_points BOOLEAN NOT NULL DEFAULT FALSE,
    lock_due_dates BOOLEAN NOT NULL DEFAULT FALSE,
    lock_availability_dates BOOLEAN NOT NULL DEFAULT FALSE,
    lock_settings BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_blueprint_content_lock UNIQUE (blueprint_course_id, object_type, object_id),
    CONSTRAINT ck_blueprint_content_lock_type CHECK (object_type IN ('CONTENT','ASSIGNMENT','ASSESSMENT','DISCUSSION','MODULE','FILE','ANNOUNCEMENT','RUBRIC','SETTINGS'))
);

CREATE TABLE blueprint_sync_run (
    sync_run_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    blueprint_course_id UUID NOT NULL,
    initiated_by UUID,
    sync_comment TEXT,
    include_course_settings BOOLEAN NOT NULL DEFAULT FALSE,
    notify_users BOOLEAN NOT NULL DEFAULT FALSE,
    run_status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    changes JSONB,
    courses_synced INT NOT NULL DEFAULT 0,
    exceptions JSONB,
    date_start TIMESTAMPTZ,
    date_end TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_blueprint_sync_run_status CHECK (run_status IN ('QUEUED','RUNNING','COMPLETED','COMPLETED_WITH_EXCEPTIONS','FAILED'))
);

CREATE TABLE course_navigation_item (
    navigation_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    tool_key VARCHAR(100) NOT NULL,
    label VARCHAR(100),
    external_tool_id UUID,
    display_order INT NOT NULL DEFAULT 0,
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_navigation_item UNIQUE (course_id, tool_key)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_course_category_tenant ON course_category(tenant_id);
CREATE INDEX idx_course_category_parent ON course_category(parent_category_id);
CREATE INDEX idx_course_category_is_active ON course_category(is_active);
CREATE INDEX idx_course_tenant ON course(tenant_id);
CREATE INDEX idx_course_code ON course(course_code);
CREATE INDEX idx_course_category ON course(category_id);
CREATE INDEX idx_course_level ON course(course_level);
CREATE INDEX idx_course_type ON course(course_type);
CREATE INDEX idx_course_is_active ON course(is_active);
CREATE INDEX idx_course_section_tenant ON course_section(tenant_id);
CREATE INDEX idx_course_section_course ON course_section(course_id);
CREATE INDEX idx_course_section_status ON course_section(status);
CREATE INDEX idx_course_section_is_open ON course_section(is_open_for_enrollment);
CREATE INDEX idx_course_section_dates ON course_section(date_start,date_end);
CREATE INDEX idx_course_module_section ON course_module(section_id);
CREATE INDEX idx_course_module_is_published ON course_module(is_published);
CREATE INDEX idx_course_module_dates ON course_module(date_start,date_end);
CREATE INDEX idx_course_instructor_section ON course_instructor(section_id);
CREATE INDEX idx_course_instructor_user ON course_instructor(user_id);
CREATE INDEX idx_course_instructor_role ON course_instructor(role);
CREATE INDEX idx_course_instructor_primary ON course_instructor(is_primary_instructor) WHERE is_primary_instructor = TRUE;
CREATE INDEX idx_course_prerequisite_course ON course_prerequisite(course_id);
CREATE INDEX idx_course_prerequisite_req ON course_prerequisite(prerequisite_course_id);
CREATE INDEX idx_course_prerequisite_type ON course_prerequisite(prerequisite_type);
CREATE INDEX idx_course_schedule_section ON course_schedule(section_id);
CREATE INDEX idx_course_schedule_day ON course_schedule(day_of_week);
CREATE INDEX idx_course_schedule_meeting_type ON course_schedule(meeting_type);
CREATE INDEX idx_course_schedule_location ON course_schedule(location);
CREATE INDEX idx_learning_outcome_course ON learning_outcome(course_id);
CREATE INDEX idx_learning_outcome_bloom ON learning_outcome(bloom_level);
CREATE INDEX idx_learning_outcome_is_primary ON learning_outcome(is_primary) WHERE is_primary = TRUE;
CREATE INDEX idx_learning_outcome_is_active ON learning_outcome(is_active);
CREATE UNIQUE INDEX uk_course_instructor_primary ON course_instructor(section_id) WHERE is_primary_instructor = TRUE;
CREATE UNIQUE INDEX uk_course_sis_source ON course(tenant_id, sis_source_id) WHERE sis_source_id IS NOT NULL;
CREATE UNIQUE INDEX uk_course_section_sis_source ON course_section(tenant_id, sis_source_id) WHERE sis_source_id IS NOT NULL;
CREATE INDEX idx_course_section_term ON course_section(term_id);
CREATE INDEX idx_course_module_course ON course_module(course_id, display_order);
CREATE UNIQUE INDEX uk_course_self_enroll_code ON course(tenant_id, self_enrollment_code) WHERE self_enrollment_code IS NOT NULL;
CREATE INDEX idx_course_kind ON course(tenant_id, course_kind);
CREATE INDEX idx_course_module_tenant ON course_module(tenant_id);
CREATE INDEX idx_course_schedule_tenant ON course_schedule(tenant_id);
CREATE INDEX idx_learning_outcome_tenant ON learning_outcome(tenant_id);
CREATE INDEX idx_org_unit_parent ON org_unit(parent_org_unit_id);
CREATE UNIQUE INDEX uk_blueprint_association_one_parent ON blueprint_association(associated_course_id) WHERE is_active = TRUE;
CREATE INDEX idx_academic_term_calendar_id_fk ON academic_term(calendar_id);
CREATE INDEX idx_course_grading_scale_id_fk ON course(grading_scale_id);
CREATE INDEX idx_course_instructor_tenant_id_fk ON course_instructor(tenant_id);
CREATE INDEX idx_course_org_unit_id_fk ON course(org_unit_id);
CREATE INDEX idx_course_prerequisite_tenant_id_fk ON course_prerequisite(tenant_id);
CREATE INDEX idx_course_schedule_instructor_assignment_id_fk ON course_schedule(instructor_assignment_id);
CREATE INDEX idx_learning_outcome_org_unit_id_fk ON learning_outcome(org_unit_id);
CREATE INDEX idx_blueprint_association_tenant_id_fk ON blueprint_association(tenant_id);
CREATE INDEX idx_blueprint_content_lock_tenant_id_fk ON blueprint_content_lock(tenant_id);
CREATE INDEX idx_blueprint_sync_run_blueprint_course_id_fk ON blueprint_sync_run(blueprint_course_id);
CREATE INDEX idx_blueprint_sync_run_initiated_by_fk ON blueprint_sync_run(initiated_by);
CREATE INDEX idx_blueprint_sync_run_tenant_id_fk ON blueprint_sync_run(tenant_id);
CREATE INDEX idx_course_navigation_item_external_tool_id_fk ON course_navigation_item(external_tool_id);
CREATE INDEX idx_course_navigation_item_tenant_id_fk ON course_navigation_item(tenant_id);
CREATE INDEX idx_course_section_crosslisted_from_course_id_fk ON course_section(crosslisted_from_course_id);
CREATE INDEX idx_course_term_id_fk ON course(term_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_course_category_date_updated BEFORE UPDATE ON course_category FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_course_date_updated BEFORE UPDATE ON course FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_course_section_date_updated BEFORE UPDATE ON course_section FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_course_module_date_updated BEFORE UPDATE ON course_module FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_learning_outcome_date_updated BEFORE UPDATE ON learning_outcome FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_org_unit_date_updated BEFORE UPDATE ON org_unit FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_academic_term_date_updated BEFORE UPDATE ON academic_term FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_course_navigation_item_date_updated BEFORE UPDATE ON course_navigation_item FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE course_category ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_category FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_category_tenant ON course_category FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course ENABLE ROW LEVEL SECURITY;
ALTER TABLE course FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_tenant ON course FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course_section ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_section FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_section_tenant ON course_section FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course_module ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_module FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_module_tenant ON course_module FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course_instructor ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_instructor FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_instructor_tenant ON course_instructor FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course_prerequisite ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_prerequisite FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_prerequisite_tenant ON course_prerequisite FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course_schedule ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_schedule FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_schedule_tenant ON course_schedule FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE learning_outcome ENABLE ROW LEVEL SECURITY;
ALTER TABLE learning_outcome FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_learning_outcome_tenant ON learning_outcome FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE org_unit ENABLE ROW LEVEL SECURITY;
ALTER TABLE org_unit FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_org_unit_tenant ON org_unit FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE academic_term ENABLE ROW LEVEL SECURITY;
ALTER TABLE academic_term FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_academic_term_tenant ON academic_term FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE blueprint_association ENABLE ROW LEVEL SECURITY;
ALTER TABLE blueprint_association FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_blueprint_association_tenant ON blueprint_association FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE blueprint_content_lock ENABLE ROW LEVEL SECURITY;
ALTER TABLE blueprint_content_lock FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_blueprint_content_lock_tenant ON blueprint_content_lock FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE blueprint_sync_run ENABLE ROW LEVEL SECURITY;
ALTER TABLE blueprint_sync_run FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_blueprint_sync_run_tenant ON blueprint_sync_run FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course_navigation_item ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_navigation_item FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_navigation_item_tenant ON course_navigation_item FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
