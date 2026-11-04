-- ================================================================================
-- 07. CONTENT DELIVERY & LEARNING ACTIVITY (5 tables)
-- ================================================================================
-- Tables: content_delivery_tracking, student_content_progress, video_watch_progress, learning_event, student_learning_session
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- ContentDeliveryTracking: Track how content is delivered to students
CREATE TABLE content_delivery_tracking (
    tracking_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    section_id UUID NOT NULL,
    content_id UUID NOT NULL,
    total_students_assigned INT DEFAULT 0,
    total_students_accessed INT DEFAULT 0,
    total_views INT DEFAULT 0,
    avg_completion_percent NUMERIC(5,2) DEFAULT 0,
    avg_time_spent_minutes NUMERIC(8,2) DEFAULT 0,
    first_accessed TIMESTAMPTZ,
    last_accessed TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_content_delivery_tracking UNIQUE (section_id, content_id),
    CONSTRAINT ck_content_delivery_tracking_completion CHECK (avg_completion_percent >= 0 AND avg_completion_percent <= 100)
);

-- StudentContentProgress: Individual student progress on content
CREATE TABLE student_content_progress (
    progress_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    content_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    percent_complete NUMERIC(5,2) DEFAULT 0,
    time_spent_minutes INT DEFAULT 0,
    view_count INT DEFAULT 0,
    date_first_accessed TIMESTAMPTZ,
    date_last_accessed TIMESTAMPTZ,
    date_completed TIMESTAMPTZ,
    last_interaction_type VARCHAR(50),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_student_content_progress UNIQUE (user_id, content_id),
    CONSTRAINT ck_student_content_progress_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'REVIEWED', 'SKIPPED', 'UNAVAILABLE')),
    CONSTRAINT ck_student_content_progress_percent CHECK (percent_complete >= 0 AND percent_complete <= 100)
);

-- VideoWatchProgress: Detailed video viewing progress (chapter by chapter)
CREATE TABLE video_watch_progress (
    watch_progress_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    video_id UUID NOT NULL,
    total_duration_seconds INT,
    current_position_seconds INT DEFAULT 0,
    percent_watched NUMERIC(5,2) DEFAULT 0,
    watch_count INT DEFAULT 1,
    total_watch_time_seconds INT DEFAULT 0,
    date_first_watched TIMESTAMPTZ,
    date_last_watched TIMESTAMPTZ,
    is_completed BOOLEAN DEFAULT FALSE,
    playback_speed NUMERIC(2,1) DEFAULT 1.0,
    captions_enabled BOOLEAN DEFAULT FALSE,
    is_seeking BOOLEAN DEFAULT FALSE,
    quality_watched VARCHAR(20),
    device_type VARCHAR(50),
    has_paused_at JSONB,
    has_rewound_at JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_video_watch_progress UNIQUE (user_id, video_id),
    CONSTRAINT ck_video_watch_progress_percent CHECK (percent_watched >= 0 AND percent_watched <= 100),
    CONSTRAINT ck_video_watch_progress_playback_speed CHECK (playback_speed >= 0.5 AND playback_speed <= 2.0)
);

-- LearningEvent: Individual learning events (page views, interactions, etc.)
CREATE TABLE learning_event (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    stud_session_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id UUID,
    action_details JSONB,
    ip_address INET,
    user_agent TEXT,
    device_type VARCHAR(50),
    browser VARCHAR(100),
    operating_system VARCHAR(100),
    duration_seconds INT,
    date_event TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_learning_event_type CHECK (event_type IN ('PAGE_VIEW', 'CONTENT_ACCESSED', 'VIDEO_PLAYED', 'VIDEO_PAUSED', 'VIDEO_RESUMED', 'QUIZ_STARTED', 'QUIZ_SUBMITTED', 'ASSIGNMENT_VIEWED', 'ASSIGNMENT_SUBMITTED', 'DOWNLOAD', 'PRINT', 'ANNOTATE', 'DISCUSSION_POSTED', 'BOOKMARK_ADDED', 'SEARCH_PERFORMED', 'FORUM_ACCESSED')),
    CONSTRAINT ck_learning_event_resource CHECK (resource_type IS NULL OR resource_type IN ('CONTENT', 'VIDEO', 'QUIZ', 'ASSIGNMENT', 'DISCUSSION', 'EBOOK', 'ATTACHMENT', 'MODULE', 'COURSE'))
);

-- StudentLearningSession: Represents a single login/study session
CREATE TABLE student_learning_session (
    stud_session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    section_id UUID NOT NULL,
    session_start TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    session_end TIMESTAMPTZ,
    session_duration_minutes INT,
    is_active BOOLEAN DEFAULT TRUE,
    device_type VARCHAR(50),
    ip_address INET,
    user_agent TEXT,
    session_status VARCHAR(50) DEFAULT 'ACTIVE',
    total_events INT DEFAULT 0,
    pages_visited INT DEFAULT 0,
    content_accessed INT DEFAULT 0,
    videos_watched INT DEFAULT 0,
    CONSTRAINT ck_student_learning_session_status CHECK (session_status IN ('ACTIVE', 'COMPLETED', 'INTERRUPTED', 'IDLE', 'TIMED_OUT'))
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_content_delivery_tracking_section ON content_delivery_tracking(section_id);
CREATE INDEX idx_content_delivery_tracking_content ON content_delivery_tracking(content_id);
CREATE INDEX idx_content_delivery_tracking_last_accessed ON content_delivery_tracking(last_accessed DESC);
CREATE INDEX idx_student_content_progress_user ON student_content_progress(user_id);
CREATE INDEX idx_student_content_progress_content ON student_content_progress(content_id);
CREATE INDEX idx_student_content_progress_status ON student_content_progress(status);
CREATE INDEX idx_student_content_progress_date_accessed ON student_content_progress(date_last_accessed DESC);
CREATE INDEX idx_video_watch_progress_user ON video_watch_progress(user_id);
CREATE INDEX idx_video_watch_progress_video ON video_watch_progress(video_id);
CREATE INDEX idx_video_watch_progress_is_completed ON video_watch_progress(is_completed);
CREATE INDEX idx_video_watch_progress_date_watched ON video_watch_progress(date_last_watched DESC);
CREATE INDEX idx_learning_event_user ON learning_event(user_id);
CREATE INDEX idx_learning_event_session ON learning_event(stud_session_id);
CREATE INDEX idx_learning_event_type ON learning_event(event_type);
CREATE INDEX idx_learning_event_resource ON learning_event(resource_type, resource_id);
CREATE INDEX idx_learning_event_date ON learning_event(date_event DESC);
CREATE INDEX idx_learning_event_user_date ON learning_event(user_id, date_event DESC);
CREATE INDEX idx_student_learning_session_user ON student_learning_session(user_id);
CREATE INDEX idx_student_learning_session_section ON student_learning_session(section_id);
CREATE INDEX idx_student_learning_session_is_active ON student_learning_session(is_active);
CREATE INDEX idx_student_learning_session_start ON student_learning_session(session_start DESC);
CREATE INDEX idx_content_delivery_tracking_tenant ON content_delivery_tracking(tenant_id);
CREATE INDEX idx_student_content_progress_tenant ON student_content_progress(tenant_id);
CREATE INDEX idx_video_watch_progress_tenant ON video_watch_progress(tenant_id);
CREATE INDEX idx_learning_event_tenant ON learning_event(tenant_id);
CREATE INDEX idx_student_learning_session_tenant ON student_learning_session(tenant_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_content_delivery_tracking_date_updated BEFORE UPDATE ON content_delivery_tracking FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_student_content_progress_date_updated BEFORE UPDATE ON student_content_progress FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_video_watch_progress_date_updated BEFORE UPDATE ON video_watch_progress FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE content_delivery_tracking ENABLE ROW LEVEL SECURITY;
ALTER TABLE content_delivery_tracking FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_content_delivery_tracking_tenant ON content_delivery_tracking FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE student_content_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_content_progress FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_student_content_progress_tenant ON student_content_progress FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE video_watch_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE video_watch_progress FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_video_watch_progress_tenant ON video_watch_progress FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE learning_event ENABLE ROW LEVEL SECURITY;
ALTER TABLE learning_event FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_learning_event_tenant ON learning_event FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE student_learning_session ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_learning_session FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_student_learning_session_tenant ON student_learning_session FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
