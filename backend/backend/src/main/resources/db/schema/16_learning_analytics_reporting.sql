-- ================================================================================
-- 16. LEARNING ANALYTICS & REPORTING (8 tables)
-- ================================================================================
-- Tables: analytics_dataset, engagement_metric, learner_success_indicator, course_health_check, report_template, custom_report, dashboard_widget, report_schedule
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- AnalyticsDataset: Raw analytics data collection
CREATE TABLE analytics_dataset (
    dataset_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID,
    section_id UUID,
    dataset_name VARCHAR(255) NOT NULL,
    dataset_description TEXT,
    dataset_type VARCHAR(50) NOT NULL,
    data_source VARCHAR(100),
    metric_type VARCHAR(50),
    aggregation_level VARCHAR(50),
    time_period VARCHAR(50),
    start_date DATE,
    end_date DATE,
    total_records INT,
    total_users INT,
    is_active BOOLEAN DEFAULT TRUE,
    last_collected TIMESTAMPTZ,
    collection_frequency VARCHAR(50),
    next_collection TIMESTAMPTZ,
    retention_days INT DEFAULT 365,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_analytics_dataset_name CHECK (LENGTH(dataset_name) >= 3),
    CONSTRAINT ck_analytics_dataset_type CHECK (dataset_type IN ('ENGAGEMENT', 'ACADEMIC', 'BEHAVIORAL', 'CONTENT_INTERACTION', 'ASSESSMENT', 'ATTENDANCE', 'DEMOGRAPHIC')),
    CONSTRAINT ck_analytics_dataset_frequency CHECK (collection_frequency IN ('REAL_TIME', 'HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY')),
    CONSTRAINT ck_analytics_dataset_retention CHECK (retention_days > 0)
);

-- EngagementMetric: Student engagement metrics
CREATE TABLE engagement_metric (
    metric_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    dataset_id UUID NOT NULL,
    user_id UUID NOT NULL,
    course_id UUID NOT NULL,
    section_id UUID,
    metric_date DATE NOT NULL,
    content_views INT DEFAULT 0,
    content_downloads INT DEFAULT 0,
    discussion_posts INT DEFAULT 0,
    discussion_replies INT DEFAULT 0,
    messages_sent INT DEFAULT 0,
    login_count INT DEFAULT 0,
    time_spent_minutes INT DEFAULT 0,
    assignment_submissions INT DEFAULT 0,
    assessment_attempts INT DEFAULT 0,
    video_watch_time_minutes INT DEFAULT 0,
    interaction_count INT DEFAULT 0,
    engagement_score NUMERIC(5,2),
    engagement_level VARCHAR(50),
    last_activity TIMESTAMPTZ,
    is_at_risk BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_engagement_metric UNIQUE (user_id, course_id, metric_date),
    CONSTRAINT ck_engagement_metric_score CHECK (engagement_score IS NULL OR (engagement_score >= 0 AND engagement_score <= 100)),
    CONSTRAINT ck_engagement_metric_level CHECK (engagement_level IN ('VERY_LOW', 'LOW', 'MODERATE', 'HIGH', 'VERY_HIGH'))
);

-- LearnerSuccessIndicator: Predictors of student success
CREATE TABLE learner_success_indicator (
    indicator_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    course_id UUID NOT NULL,
    section_id UUID,
    calculation_date DATE NOT NULL,
    current_grade NUMERIC(5,2),
    predicted_grade NUMERIC(5,2),
    predicted_pass_probability NUMERIC(5,2),
    predicted_completion_probability NUMERIC(5,2),
    risk_score NUMERIC(5,2),
    success_score NUMERIC(5,2),
    attendance_trend VARCHAR(50),
    assignment_trend VARCHAR(50),
    assessment_trend VARCHAR(50),
    engagement_trend VARCHAR(50),
    content_access_trend VARCHAR(50),
    collaboration_trend VARCHAR(50),
    early_warning_flag BOOLEAN DEFAULT FALSE,
    early_warning_reason VARCHAR(255),
    intervention_recommended BOOLEAN DEFAULT FALSE,
    recommended_intervention TEXT,
    time_to_intervention_hours INT,
    intervention_priority VARCHAR(50),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_learner_success_indicator_probability CHECK (predicted_pass_probability IS NULL OR (predicted_pass_probability >= 0 AND predicted_pass_probability <= 100)),
    CONSTRAINT ck_learner_success_indicator_completion CHECK (predicted_completion_probability IS NULL OR (predicted_completion_probability >= 0 AND predicted_completion_probability <= 100)),
    CONSTRAINT ck_learner_success_indicator_risk CHECK (risk_score IS NULL OR (risk_score >= 0 AND risk_score <= 100)),
    CONSTRAINT ck_learner_success_indicator_success CHECK (success_score IS NULL OR (success_score >= 0 AND success_score <= 100)),
    CONSTRAINT ck_learner_success_indicator_trend CHECK (attendance_trend IS NULL OR attendance_trend IN ('IMPROVING', 'STABLE', 'DECLINING')),
    CONSTRAINT ck_learner_success_indicator_priority CHECK (intervention_priority IS NULL OR intervention_priority IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW'))
);

-- CourseHealthCheck: Overall course analytics and health
CREATE TABLE course_health_check (
    health_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL UNIQUE,
    section_id UUID,
    assessment_date DATE NOT NULL,
    total_students INT,
    active_students INT,
    inactive_students INT,
    avg_engagement_score NUMERIC(5,2),
    avg_attendance_percentage NUMERIC(5,2),
    avg_grade NUMERIC(5,2),
    pass_rate NUMERIC(5,2),
    completion_rate NUMERIC(5,2),
    dropout_risk_count INT,
    content_completion_rate NUMERIC(5,2),
    discussion_participation_rate NUMERIC(5,2),
    assignment_submission_rate NUMERIC(5,2),
    assessment_completion_rate NUMERIC(5,2),
    instructor_response_time_hours NUMERIC(8,2),
    course_difficulty_rating NUMERIC(3,1),
    student_satisfaction_score NUMERIC(3,1),
    course_health_score NUMERIC(5,2),
    health_status VARCHAR(50),
    critical_issues JSONB,
    recommendations TEXT,
    last_assessment_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_course_health_check_score CHECK (course_health_score IS NULL OR (course_health_score >= 0 AND course_health_score <= 100)),
    CONSTRAINT ck_course_health_check_status CHECK (health_status IN ('EXCELLENT', 'GOOD', 'FAIR', 'POOR', 'CRITICAL')),
    CONSTRAINT ck_course_health_check_satisfaction CHECK (student_satisfaction_score IS NULL OR (student_satisfaction_score >= 1 AND student_satisfaction_score <= 5))
);

-- ReportTemplate: Template definitions for reports
CREATE TABLE report_template (
    template_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    template_name VARCHAR(255) NOT NULL,
    template_description TEXT,
    template_type VARCHAR(50) NOT NULL,
    report_category VARCHAR(50),
    data_source VARCHAR(100),
    dimensions JSONB,
    metrics JSONB,
    filters JSONB,
    sort_order JSONB,
    visualization_type VARCHAR(50),
    layout_config JSONB,
    is_public BOOLEAN DEFAULT FALSE,
    is_system_template BOOLEAN DEFAULT FALSE,
    created_by UUID,
    is_active BOOLEAN DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_report_template_name CHECK (LENGTH(template_name) >= 3),
    CONSTRAINT ck_report_template_type CHECK (template_type IN ('STANDARD', 'CUSTOM', 'ADHOC', 'DASHBOARD'))
);

-- CustomReport: Custom report instances
CREATE TABLE custom_report (
    report_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    template_id UUID,
    report_name VARCHAR(255) NOT NULL,
    report_description TEXT,
    report_type VARCHAR(50) NOT NULL,
    report_scope VARCHAR(50),
    scope_id UUID,
    created_by UUID NOT NULL,
    report_status VARCHAR(50) DEFAULT 'DRAFT',
    data_range_start DATE,
    data_range_end DATE,
    filters_applied JSONB,
    report_parameters JSONB,
    visualization_settings JSONB,
    generated_at TIMESTAMPTZ,
    generation_time_seconds INT,
    report_file_path VARCHAR(500),
    report_file_size_bytes INT,
    distribution_list JSONB,
    last_distributed TIMESTAMPTZ,
    view_count INT DEFAULT 0,
    last_viewed TIMESTAMPTZ,
    is_archived BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_custom_report_name CHECK (LENGTH(report_name) >= 3),
    CONSTRAINT ck_custom_report_type CHECK (report_type IN ('COURSE_ANALYTICS', 'STUDENT_PERFORMANCE', 'ENGAGEMENT', 'ENROLLMENT', 'GRADING', 'ATTENDANCE', 'SYSTEM_HEALTH')),
    CONSTRAINT ck_custom_report_status CHECK (report_status IN ('DRAFT', 'PENDING', 'GENERATING', 'READY', 'ERROR', 'ARCHIVED')),
    CONSTRAINT ck_custom_report_scope CHECK (report_scope IN ('INSTITUTION', 'DEPARTMENT', 'COURSE', 'STUDENT', 'INSTRUCTOR'))
);

-- DashboardWidget: Widget configurations for dashboards
CREATE TABLE dashboard_widget (
    widget_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    dashboard_id VARCHAR(100),
    widget_title VARCHAR(255) NOT NULL,
    widget_description TEXT,
    widget_type VARCHAR(50) NOT NULL,
    data_source VARCHAR(100),
    metric_key VARCHAR(100),
    visualization_type VARCHAR(50),
    position_row INT,
    position_column INT,
    width_units INT DEFAULT 1,
    height_units INT DEFAULT 1,
    refresh_interval_minutes INT DEFAULT 60,
    filters JSONB,
    display_settings JSONB,
    data_aggregation VARCHAR(50),
    time_period VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    last_refreshed TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_dashboard_widget_title CHECK (LENGTH(widget_title) >= 3),
    CONSTRAINT ck_dashboard_widget_type CHECK (widget_type IN ('METRIC_CARD', 'LINE_CHART', 'BAR_CHART', 'PIE_CHART', 'HEATMAP', 'TABLE', 'GAUGE', 'TIMELINE', 'SCATTER')),
    CONSTRAINT ck_dashboard_widget_refresh CHECK (refresh_interval_minutes > 0)
);

-- ReportSchedule: Scheduled report generation and distribution
CREATE TABLE report_schedule (
    schedule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    report_id UUID NOT NULL,
    scheduled_by UUID NOT NULL,
    schedule_name VARCHAR(255),
    schedule_frequency VARCHAR(50) NOT NULL,
    schedule_time TIME,
    schedule_day_of_week INT,
    schedule_day_of_month INT,
    schedule_timezone VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    next_run_at TIMESTAMPTZ,
    last_run_at TIMESTAMPTZ,
    last_run_status VARCHAR(50),
    run_count INT DEFAULT 0,
    failure_count INT DEFAULT 0,
    distribution_enabled BOOLEAN DEFAULT FALSE,
    distribution_recipients JSONB,
    distribution_format VARCHAR(50),
    notification_on_failure BOOLEAN DEFAULT TRUE,
    notification_recipients JSONB,
    end_date DATE,
    max_runs INT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_report_schedule_frequency CHECK (schedule_frequency IN ('HOURLY', 'DAILY', 'WEEKLY', 'BIWEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY')),
    CONSTRAINT ck_report_schedule_day_of_week CHECK (schedule_day_of_week IS NULL OR (schedule_day_of_week >= 0 AND schedule_day_of_week <= 6)),
    CONSTRAINT ck_report_schedule_day_of_month CHECK (schedule_day_of_month IS NULL OR (schedule_day_of_month >= 1 AND schedule_day_of_month <= 31)),
    CONSTRAINT ck_report_schedule_distribution_format CHECK (distribution_format IN ('PDF', 'CSV', 'EXCEL', 'JSON', 'HTML'))
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_analytics_dataset_tenant ON analytics_dataset(tenant_id);
CREATE INDEX idx_analytics_dataset_course ON analytics_dataset(course_id);
CREATE INDEX idx_analytics_dataset_type ON analytics_dataset(dataset_type);
CREATE INDEX idx_analytics_dataset_last_collected ON analytics_dataset(last_collected DESC);
CREATE INDEX idx_analytics_dataset_is_active ON analytics_dataset(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_engagement_metric_dataset ON engagement_metric(dataset_id);
CREATE INDEX idx_engagement_metric_user ON engagement_metric(user_id);
CREATE INDEX idx_engagement_metric_course ON engagement_metric(course_id);
CREATE INDEX idx_engagement_metric_date ON engagement_metric(metric_date DESC);
CREATE INDEX idx_engagement_metric_is_at_risk ON engagement_metric(is_at_risk) WHERE is_at_risk = TRUE;
CREATE INDEX idx_engagement_metric_level ON engagement_metric(engagement_level);
CREATE INDEX idx_learner_success_indicator_user ON learner_success_indicator(user_id);
CREATE INDEX idx_learner_success_indicator_course ON learner_success_indicator(course_id);
CREATE INDEX idx_learner_success_indicator_calculation_date ON learner_success_indicator(calculation_date DESC);
CREATE INDEX idx_learner_success_indicator_early_warning ON learner_success_indicator(early_warning_flag) WHERE early_warning_flag = TRUE;
CREATE INDEX idx_learner_success_indicator_intervention ON learner_success_indicator(intervention_recommended) WHERE intervention_recommended = TRUE;
CREATE INDEX idx_course_health_check_assessment_date ON course_health_check(assessment_date DESC);
CREATE INDEX idx_course_health_check_health_status ON course_health_check(health_status);
CREATE INDEX idx_report_template_tenant ON report_template(tenant_id);
CREATE INDEX idx_report_template_type ON report_template(template_type);
CREATE INDEX idx_report_template_created_by ON report_template(created_by);
CREATE INDEX idx_report_template_is_public ON report_template(is_public) WHERE is_public = TRUE;
CREATE INDEX idx_custom_report_tenant ON custom_report(tenant_id);
CREATE INDEX idx_custom_report_template ON custom_report(template_id);
CREATE INDEX idx_custom_report_created_by ON custom_report(created_by);
CREATE INDEX idx_custom_report_status ON custom_report(report_status);
CREATE INDEX idx_custom_report_date_created ON custom_report(date_created DESC);
CREATE INDEX idx_custom_report_is_archived ON custom_report(is_archived) WHERE is_archived = FALSE;
CREATE INDEX idx_dashboard_widget_tenant ON dashboard_widget(tenant_id);
CREATE INDEX idx_dashboard_widget_user ON dashboard_widget(user_id);
CREATE INDEX idx_dashboard_widget_dashboard ON dashboard_widget(dashboard_id);
CREATE INDEX idx_dashboard_widget_type ON dashboard_widget(widget_type);
CREATE INDEX idx_dashboard_widget_is_active ON dashboard_widget(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_report_schedule_report ON report_schedule(report_id);
CREATE INDEX idx_report_schedule_scheduled_by ON report_schedule(scheduled_by);
CREATE INDEX idx_report_schedule_is_active ON report_schedule(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_report_schedule_next_run ON report_schedule(next_run_at);
CREATE INDEX idx_report_schedule_last_run ON report_schedule(last_run_at DESC);
CREATE INDEX idx_engagement_metric_tenant ON engagement_metric(tenant_id);
CREATE INDEX idx_learner_success_indicator_tenant ON learner_success_indicator(tenant_id);
CREATE INDEX idx_course_health_check_tenant ON course_health_check(tenant_id);
CREATE INDEX idx_report_schedule_tenant ON report_schedule(tenant_id);
CREATE INDEX idx_analytics_dataset_section_id_fk ON analytics_dataset(section_id);
CREATE INDEX idx_course_health_check_last_assessment_by_fk ON course_health_check(last_assessment_by);
CREATE INDEX idx_course_health_check_section_id_fk ON course_health_check(section_id);
CREATE INDEX idx_engagement_metric_section_id_fk ON engagement_metric(section_id);
CREATE INDEX idx_learner_success_indicator_section_id_fk ON learner_success_indicator(section_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_analytics_dataset_date_updated BEFORE UPDATE ON analytics_dataset FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_learner_success_indicator_date_updated BEFORE UPDATE ON learner_success_indicator FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_course_health_check_date_updated BEFORE UPDATE ON course_health_check FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_report_template_date_updated BEFORE UPDATE ON report_template FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_custom_report_date_updated BEFORE UPDATE ON custom_report FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_dashboard_widget_date_updated BEFORE UPDATE ON dashboard_widget FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_report_schedule_date_updated BEFORE UPDATE ON report_schedule FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE analytics_dataset ENABLE ROW LEVEL SECURITY;
ALTER TABLE analytics_dataset FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_analytics_dataset_tenant ON analytics_dataset FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE engagement_metric ENABLE ROW LEVEL SECURITY;
ALTER TABLE engagement_metric FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_engagement_metric_tenant ON engagement_metric FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE learner_success_indicator ENABLE ROW LEVEL SECURITY;
ALTER TABLE learner_success_indicator FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_learner_success_indicator_tenant ON learner_success_indicator FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course_health_check ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_health_check FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_health_check_tenant ON course_health_check FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE report_template ENABLE ROW LEVEL SECURITY;
ALTER TABLE report_template FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_report_template_tenant ON report_template FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE custom_report ENABLE ROW LEVEL SECURITY;
ALTER TABLE custom_report FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_custom_report_tenant ON custom_report FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE dashboard_widget ENABLE ROW LEVEL SECURITY;
ALTER TABLE dashboard_widget FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_dashboard_widget_tenant ON dashboard_widget FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE report_schedule ENABLE ROW LEVEL SECURITY;
ALTER TABLE report_schedule FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_report_schedule_tenant ON report_schedule FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
