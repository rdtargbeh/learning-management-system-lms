-- ================================================================================
-- 06. E-BOOK PLATFORM (11 tables)
-- ================================================================================
-- Tables: ebook, ebook_file, ebook_chapter, ebook_chapter_assignment, user_ebook_access, ebook_reading_progress, ebook_annotation, ebook_chapter_assessment, user_chapter_assessment_attempt, ebook_usage_analytics, ebook_search
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- Ebook: Digital textbooks (instructor uploaded or integrated from third-party)
CREATE TABLE ebook (
    ebook_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    ebook_title VARCHAR(255) NOT NULL,
    ebook_description TEXT,
    author VARCHAR(255),
    publisher VARCHAR(255),
    isbn VARCHAR(20),
    publication_year INT,
    cover_image_url VARCHAR(500),
    language VARCHAR(10) DEFAULT 'en',
    source_type VARCHAR(50) NOT NULL,
    third_party_provider VARCHAR(100),
    third_party_id VARCHAR(255),
    third_party_access_url VARCHAR(500),
    is_required_reading BOOLEAN DEFAULT FALSE,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT DEFAULT 0,
    date_assigned DATE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_ebook_title CHECK (LENGTH(ebook_title) >= 3),
    CONSTRAINT ck_ebook_source CHECK (source_type IN ('UPLOADED', 'THIRD_PARTY_INTEGRATED')),
    CONSTRAINT ck_ebook_year CHECK (publication_year IS NULL OR publication_year >= 1800),
    CONSTRAINT ck_ebook_third_party CHECK ( (source_type = 'UPLOADED') OR (source_type = 'THIRD_PARTY_INTEGRATED' AND third_party_provider IS NOT NULL AND third_party_id IS NOT NULL) )
);

-- EbookFile: Actual book files (PDF, ePub, etc.) for uploaded ebooks
CREATE TABLE ebook_file (
    file_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    ebook_id UUID NOT NULL,
    file_type VARCHAR(50),
    file_url VARCHAR(500) NOT NULL,
    storage_path VARCHAR(500),
    file_size_bytes BIGINT,
    mime_type VARCHAR(100),
    total_pages INT,
    is_primary BOOLEAN DEFAULT TRUE,
    uploaded_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_ebook_file_type CHECK (file_type IN ('PDF', 'EPUB', 'HTML', 'MOBI', 'AZW3')),
    CONSTRAINT ck_ebook_file_size CHECK (file_size_bytes > 0)
);

-- EbookChapter: Chapters/sections within ebook
CREATE TABLE ebook_chapter (
    chapter_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    ebook_id UUID NOT NULL,
    chapter_number INT NOT NULL,
    chapter_title VARCHAR(255) NOT NULL,
    chapter_description TEXT,
    page_start INT,
    page_end INT,
    estimated_reading_time_minutes INT,
    learning_objectives TEXT,
    key_concepts TEXT,
    display_order INT DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ebook_chapter_number UNIQUE (ebook_id, chapter_number),
    CONSTRAINT ck_ebook_chapter_title CHECK (LENGTH(chapter_title) >= 3),
    CONSTRAINT ck_ebook_chapter_pages CHECK (page_end IS NULL OR page_end >= page_start)
);

-- EbookChapterAssignment: Instructor assigns chapters to students (required reading)
CREATE TABLE ebook_chapter_assignment (
    assignment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    section_id UUID NOT NULL,
    chapter_id UUID NOT NULL,
    assignment_order INT NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    date_assigned DATE NOT NULL,
    date_due DATE,
    completion_required BOOLEAN DEFAULT FALSE,
    assessment_required BOOLEAN DEFAULT FALSE,
    instructions TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ebook_chapter_assignment UNIQUE (section_id, chapter_id),
    CONSTRAINT ck_ebook_chapter_assignment_order CHECK (assignment_order > 0)
);

-- UserEbookAccess: Students' access to ebooks
CREATE TABLE user_ebook_access (
    access_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    ebook_id UUID NOT NULL,
    user_id UUID NOT NULL,
    access_type VARCHAR(50) NOT NULL,
    date_granted DATE NOT NULL,
    date_expires DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_ebook_access UNIQUE (ebook_id, user_id),
    CONSTRAINT ck_user_ebook_access_type CHECK (access_type IN ('ENROLLED', 'SHARED', 'GUEST')),
    CONSTRAINT ck_user_ebook_access_dates CHECK (date_expires IS NULL OR date_expires >= date_granted)
);

-- EbookReadingProgress: Track student reading progress at chapter level
CREATE TABLE ebook_reading_progress (
    progress_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    chapter_id UUID NOT NULL,
    pages_read INT DEFAULT 0,
    total_pages INT,
    percent_complete NUMERIC(5,2) DEFAULT 0,
    reading_time_minutes INT DEFAULT 0,
    last_page_read INT,
    last_position_percent NUMERIC(5,2),
    is_completed BOOLEAN DEFAULT FALSE,
    date_started TIMESTAMPTZ,
    date_completed TIMESTAMPTZ,
    last_accessed TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ebook_reading_progress UNIQUE (user_id, chapter_id),
    CONSTRAINT ck_ebook_reading_progress_percent CHECK (percent_complete >= 0 AND percent_complete <= 100),
    CONSTRAINT ck_ebook_reading_progress_position CHECK (last_position_percent IS NULL OR (last_position_percent >= 0 AND last_position_percent <= 100))
);

-- EbookAnnotation: Student highlights, bookmarks, notes
CREATE TABLE ebook_annotation (
    annotation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    chapter_id UUID NOT NULL,
    annotation_type VARCHAR(50),
    page_number INT,
    page_position_percent NUMERIC(5,2),
    highlighted_text TEXT,
    annotation_note TEXT,
    color VARCHAR(7),
    is_private BOOLEAN DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_ebook_annotation_type CHECK (annotation_type IN ('HIGHLIGHT', 'NOTE', 'BOOKMARK', 'ANNOTATION')),
    CONSTRAINT ck_ebook_annotation_position CHECK (page_position_percent IS NULL OR (page_position_percent >= 0 AND page_position_percent <= 100))
);

-- EbookChapterAssessment: Mini assessments at end of chapters
CREATE TABLE ebook_chapter_assessment (
    ebook_assessment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    chapter_id UUID NOT NULL,
    assessment_title VARCHAR(255) NOT NULL,
    assessment_description TEXT,
    assessment_type VARCHAR(50),
    min_score_to_pass NUMERIC(5,2) DEFAULT 70.0,
    allow_multiple_attempts BOOLEAN DEFAULT TRUE,
    max_attempts INT,
    time_limit_minutes INT,
    show_correct_answers BOOLEAN DEFAULT TRUE,
    show_score_immediately BOOLEAN DEFAULT FALSE,
    is_required_to_progress BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_ebook_chapter_assessment_type CHECK (assessment_type IN ('QUIZ', 'READING_CHECK', 'COMPREHENSION_TEST', 'DISCUSSION', 'REFLECTION')),
    CONSTRAINT ck_ebook_chapter_assessment_pass_score CHECK (min_score_to_pass >= 0 AND min_score_to_pass <= 100)
);

-- UserChapterAssessmentAttempt: Student's assessment attempts
CREATE TABLE user_chapter_assessment_attempt (
    attempt_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    ebook_assessment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    attempt_number INT NOT NULL,
    score NUMERIC(5,2),
    passed BOOLEAN DEFAULT FALSE,
    date_started TIMESTAMPTZ NOT NULL,
    date_completed TIMESTAMPTZ,
    time_spent_minutes INT,
    CONSTRAINT uk_user_chapter_assessment_attempt UNIQUE (ebook_assessment_id, user_id, attempt_number),
    CONSTRAINT ck_user_chapter_assessment_attempt_score CHECK (score IS NULL OR (score >= 0 AND score <= 100))
);

-- EbookUsageAnalytics: Aggregate analytics for instructors
CREATE TABLE ebook_usage_analytics (
    analytics_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    section_id UUID NOT NULL,
    ebook_id UUID NOT NULL,
    total_students_assigned INT DEFAULT 0,
    students_accessed INT DEFAULT 0,
    students_completed INT DEFAULT 0,
    avg_reading_time_minutes NUMERIC(8,2) DEFAULT 0,
    avg_completion_percent NUMERIC(5,2) DEFAULT 0,
    most_read_chapter_id UUID,
    least_read_chapter_id UUID,
    avg_assessment_score NUMERIC(5,2),
    last_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ebook_usage_analytics UNIQUE (section_id, ebook_id)
);

-- EbookSearch: Full-text search index for book content
CREATE TABLE ebook_search (
    search_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    ebook_id UUID NOT NULL,
    chapter_id UUID,
    page_number INT,
    searchable_text TEXT,
    date_indexed TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_ebook_course ON ebook(course_id);
CREATE INDEX idx_ebook_is_published ON ebook(is_published);
CREATE INDEX idx_ebook_is_required ON ebook(is_required_reading);
CREATE INDEX idx_ebook_display_order ON ebook(course_id, display_order);
CREATE INDEX idx_ebook_file_ebook ON ebook_file(ebook_id);
CREATE INDEX idx_ebook_file_type ON ebook_file(file_type);
CREATE INDEX idx_ebook_file_is_primary ON ebook_file(is_primary);
CREATE INDEX idx_ebook_chapter_ebook ON ebook_chapter(ebook_id);
CREATE INDEX idx_ebook_chapter_display_order ON ebook_chapter(ebook_id, display_order);
CREATE INDEX idx_ebook_chapter_is_published ON ebook_chapter(is_published);
CREATE INDEX idx_ebook_chapter_assignment_section ON ebook_chapter_assignment(section_id);
CREATE INDEX idx_ebook_chapter_assignment_chapter ON ebook_chapter_assignment(chapter_id);
CREATE INDEX idx_ebook_chapter_assignment_is_required ON ebook_chapter_assignment(is_required);
CREATE INDEX idx_ebook_chapter_assignment_due ON ebook_chapter_assignment(date_due);
CREATE INDEX idx_user_ebook_access_ebook ON user_ebook_access(ebook_id);
CREATE INDEX idx_user_ebook_access_user ON user_ebook_access(user_id);
CREATE INDEX idx_user_ebook_access_is_active ON user_ebook_access(is_active);
CREATE INDEX idx_ebook_reading_progress_user ON ebook_reading_progress(user_id);
CREATE INDEX idx_ebook_reading_progress_chapter ON ebook_reading_progress(chapter_id);
CREATE INDEX idx_ebook_reading_progress_is_completed ON ebook_reading_progress(is_completed);
CREATE INDEX idx_ebook_reading_progress_last_accessed ON ebook_reading_progress(last_accessed DESC);
CREATE INDEX idx_ebook_annotation_user ON ebook_annotation(user_id);
CREATE INDEX idx_ebook_annotation_chapter ON ebook_annotation(chapter_id);
CREATE INDEX idx_ebook_annotation_type ON ebook_annotation(annotation_type);
CREATE INDEX idx_ebook_annotation_date ON ebook_annotation(date_created DESC);
CREATE INDEX idx_ebook_chapter_assessment_chapter ON ebook_chapter_assessment(chapter_id);
CREATE INDEX idx_ebook_chapter_assessment_is_required ON ebook_chapter_assessment(is_required_to_progress);
CREATE INDEX idx_ebook_chapter_assessment_is_published ON ebook_chapter_assessment(is_published);
CREATE INDEX idx_user_chapter_assessment_attempt_assessment ON user_chapter_assessment_attempt(ebook_assessment_id);
CREATE INDEX idx_user_chapter_assessment_attempt_user ON user_chapter_assessment_attempt(user_id);
CREATE INDEX idx_user_chapter_assessment_attempt_passed ON user_chapter_assessment_attempt(passed);
CREATE INDEX idx_ebook_usage_analytics_section ON ebook_usage_analytics(section_id);
CREATE INDEX idx_ebook_usage_analytics_ebook ON ebook_usage_analytics(ebook_id);
CREATE INDEX idx_ebook_search_ebook ON ebook_search(ebook_id);
CREATE INDEX idx_ebook_search_chapter ON ebook_search(chapter_id);
CREATE INDEX idx_ebook_tenant ON ebook(tenant_id);
CREATE INDEX idx_ebook_file_tenant ON ebook_file(tenant_id);
CREATE INDEX idx_ebook_chapter_tenant ON ebook_chapter(tenant_id);
CREATE INDEX idx_ebook_chapter_assignment_tenant ON ebook_chapter_assignment(tenant_id);
CREATE INDEX idx_user_ebook_access_tenant ON user_ebook_access(tenant_id);
CREATE INDEX idx_ebook_reading_progress_tenant ON ebook_reading_progress(tenant_id);
CREATE INDEX idx_ebook_annotation_tenant ON ebook_annotation(tenant_id);
CREATE INDEX idx_ebook_chapter_assessment_tenant ON ebook_chapter_assessment(tenant_id);
CREATE INDEX idx_user_chapter_assessment_attempt_tenant ON user_chapter_assessment_attempt(tenant_id);
CREATE INDEX idx_ebook_usage_analytics_tenant ON ebook_usage_analytics(tenant_id);
CREATE INDEX idx_ebook_search_tenant ON ebook_search(tenant_id);
CREATE INDEX idx_ebook_file_uploaded_by_fk ON ebook_file(uploaded_by);
CREATE INDEX idx_ebook_usage_analytics_least_read_chapter_id_fk ON ebook_usage_analytics(least_read_chapter_id);
CREATE INDEX idx_ebook_usage_analytics_most_read_chapter_id_fk ON ebook_usage_analytics(most_read_chapter_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_ebook_date_updated BEFORE UPDATE ON ebook FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_ebook_chapter_date_updated BEFORE UPDATE ON ebook_chapter FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_ebook_reading_progress_date_updated BEFORE UPDATE ON ebook_reading_progress FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_ebook_annotation_date_updated BEFORE UPDATE ON ebook_annotation FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_ebook_chapter_assessment_date_updated BEFORE UPDATE ON ebook_chapter_assessment FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE ebook ENABLE ROW LEVEL SECURITY;
ALTER TABLE ebook FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_ebook_tenant ON ebook FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE ebook_file ENABLE ROW LEVEL SECURITY;
ALTER TABLE ebook_file FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_ebook_file_tenant ON ebook_file FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE ebook_chapter ENABLE ROW LEVEL SECURITY;
ALTER TABLE ebook_chapter FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_ebook_chapter_tenant ON ebook_chapter FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE ebook_chapter_assignment ENABLE ROW LEVEL SECURITY;
ALTER TABLE ebook_chapter_assignment FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_ebook_chapter_assignment_tenant ON ebook_chapter_assignment FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE user_ebook_access ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_ebook_access FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_user_ebook_access_tenant ON user_ebook_access FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE ebook_reading_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE ebook_reading_progress FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_ebook_reading_progress_tenant ON ebook_reading_progress FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE ebook_annotation ENABLE ROW LEVEL SECURITY;
ALTER TABLE ebook_annotation FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_ebook_annotation_tenant ON ebook_annotation FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE ebook_chapter_assessment ENABLE ROW LEVEL SECURITY;
ALTER TABLE ebook_chapter_assessment FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_ebook_chapter_assessment_tenant ON ebook_chapter_assessment FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE user_chapter_assessment_attempt ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_chapter_assessment_attempt FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_user_chapter_assessment_attempt_tenant ON user_chapter_assessment_attempt FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE ebook_usage_analytics ENABLE ROW LEVEL SECURITY;
ALTER TABLE ebook_usage_analytics FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_ebook_usage_analytics_tenant ON ebook_usage_analytics FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE ebook_search ENABLE ROW LEVEL SECURITY;
ALTER TABLE ebook_search FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_ebook_search_tenant ON ebook_search FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
