-- ================================================================================
-- 13. ANNOUNCEMENTS & NOTIFICATIONS (2 tables)
-- ================================================================================
-- Tables: announcement, announcement_view
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- Announcement: Course and system announcements
CREATE TABLE announcement (
    announcement_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID,
    section_id UUID,
    module_id UUID,
    announcement_title VARCHAR(255) NOT NULL,
    announcement_content TEXT NOT NULL,
    announcement_html JSONB,
    announcement_type VARCHAR(50) NOT NULL,
    created_by UUID NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    is_pinned BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    start_display_date DATE,
    end_display_date DATE,
    delay_posting BOOLEAN DEFAULT FALSE,
    delayed_until TIMESTAMPTZ,
    require_read_confirmation BOOLEAN DEFAULT FALSE,
    read_confirmation_deadline TIMESTAMPTZ,
    allow_comments BOOLEAN DEFAULT TRUE,
    moderate_comments BOOLEAN DEFAULT FALSE,
    send_notifications BOOLEAN DEFAULT TRUE,
    notification_sent_at TIMESTAMPTZ,
    target_audience VARCHAR(50),
    visibility VARCHAR(50) DEFAULT 'PUBLISHED',
    total_comments INT DEFAULT 0,
    total_views INT DEFAULT 0,
    total_reads INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_announcement_title CHECK (LENGTH(announcement_title) >= 3),
    CONSTRAINT ck_announcement_content CHECK (LENGTH(announcement_content) >= 5),
    CONSTRAINT ck_announcement_type CHECK (announcement_type IN ('GENERAL', 'ASSIGNMENT', 'GRADE', 'EMERGENCY', 'HOLIDAY', 'DEADLINE', 'COURSE_UPDATE', 'SYSTEM')),
    CONSTRAINT ck_announcement_target CHECK (target_audience IN ('ALL', 'STUDENTS', 'INSTRUCTORS', 'STAFF', 'SPECIFIC_USERS', 'SPECIFIC_ROLES')),
    CONSTRAINT ck_announcement_visibility CHECK (visibility IN ('PUBLISHED', 'DRAFT', 'SCHEDULED', 'ARCHIVED', 'DELETED')),
    CONSTRAINT ck_announcement_dates CHECK (end_display_date IS NULL OR end_display_date >= start_display_date),
    CONSTRAINT ck_announcement_delayed CHECK (delay_posting = FALSE OR delayed_until IS NOT NULL)
);

-- AnnouncementView: Track which users have viewed announcements
CREATE TABLE announcement_view (
    view_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    announcement_id UUID NOT NULL,
    user_id UUID NOT NULL,
    view_count INT DEFAULT 1,
    is_read BOOLEAN DEFAULT FALSE,
    date_first_viewed TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_last_viewed TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_read TIMESTAMPTZ,
    read_confirmation_submitted BOOLEAN DEFAULT FALSE,
    confirmation_submitted_at TIMESTAMPTZ,
    CONSTRAINT uk_announcement_view UNIQUE (announcement_id, user_id),
    CONSTRAINT ck_announcement_view_count CHECK (view_count > 0)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_announcement_tenant ON announcement(tenant_id);
CREATE INDEX idx_announcement_course ON announcement(course_id);
CREATE INDEX idx_announcement_section ON announcement(section_id);
CREATE INDEX idx_announcement_module ON announcement(module_id);
CREATE INDEX idx_announcement_created_by ON announcement(created_by);
CREATE INDEX idx_announcement_is_published ON announcement(is_published);
CREATE INDEX idx_announcement_is_pinned ON announcement(is_pinned) WHERE is_pinned = TRUE;
CREATE INDEX idx_announcement_type ON announcement(announcement_type);
CREATE INDEX idx_announcement_visibility ON announcement(visibility);
CREATE INDEX idx_announcement_display_dates ON announcement(start_display_date, end_display_date);
CREATE INDEX idx_announcement_date_created ON announcement(date_created DESC);
CREATE INDEX idx_announcement_view_announcement ON announcement_view(announcement_id);
CREATE INDEX idx_announcement_view_user ON announcement_view(user_id);
CREATE INDEX idx_announcement_view_is_read ON announcement_view(is_read) WHERE is_read = TRUE;
CREATE INDEX idx_announcement_view_date_read ON announcement_view(date_read DESC);
CREATE INDEX idx_announcement_view_confirmation ON announcement_view(read_confirmation_submitted) WHERE read_confirmation_submitted = TRUE;
CREATE INDEX idx_announcement_view_tenant ON announcement_view(tenant_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_announcement_date_updated BEFORE UPDATE ON announcement FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE announcement ENABLE ROW LEVEL SECURITY;
ALTER TABLE announcement FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_announcement_tenant ON announcement FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE announcement_view ENABLE ROW LEVEL SECURITY;
ALTER TABLE announcement_view FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_announcement_view_tenant ON announcement_view FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
