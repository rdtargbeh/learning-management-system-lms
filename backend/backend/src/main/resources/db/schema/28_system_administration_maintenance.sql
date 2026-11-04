-- ================================================================================
-- 28. SYSTEM ADMINISTRATION & MAINTENANCE (5 tables)
-- ================================================================================
-- Tables: system_config, scheduled_job, job_execution, error_log, feature_flag
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- SystemConfig: System-wide configuration settings
CREATE TABLE system_config (
    config_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    config_category VARCHAR(100) NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(50) NOT NULL,
    description TEXT,
    is_sensitive BOOLEAN DEFAULT FALSE,
    is_encrypted BOOLEAN DEFAULT FALSE,
    default_value TEXT,
    allowed_values JSONB,
    validation_rule VARCHAR(255),
    min_value NUMERIC,
    max_value NUMERIC,
    is_required BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    modified_by UUID,
    modified_at TIMESTAMPTZ,
    applied_at TIMESTAMPTZ,
    requires_restart BOOLEAN DEFAULT FALSE,
    environment VARCHAR(50),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_system_config UNIQUE (tenant_id, config_category, config_key),
    CONSTRAINT ck_system_config_category CHECK (config_category IN ('GENERAL', 'SECURITY', 'PERFORMANCE', 'EMAIL', 'STORAGE', 'AUTHENTICATION', 'INTEGRATION', 'BACKUP', 'LOGGING', 'CACHE', 'API', 'FEATURES')),
    CONSTRAINT ck_system_config_type CHECK (config_type IN ('STRING', 'INTEGER', 'BOOLEAN', 'JSON', 'URL', 'EMAIL', 'NUMERIC', 'ENUM')),
    CONSTRAINT ck_system_config_environment CHECK (environment IN ('DEVELOPMENT', 'STAGING', 'PRODUCTION', 'ALL'))
);

-- ScheduledJob: Scheduled tasks and batch jobs
CREATE TABLE scheduled_job (
    job_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    job_name VARCHAR(255) NOT NULL,
    job_description TEXT,
    job_type VARCHAR(50) NOT NULL,
    job_handler VARCHAR(500) NOT NULL,
    job_parameters JSONB,
    schedule_expression VARCHAR(100),
    schedule_type VARCHAR(50) NOT NULL,
    timezone VARCHAR(50) DEFAULT 'UTC',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    run_immediately BOOLEAN DEFAULT FALSE,
    max_concurrent_instances INT DEFAULT 1,
    current_running_count INT DEFAULT 0,
    retry_on_failure BOOLEAN DEFAULT TRUE,
    max_retry_attempts INT DEFAULT 3,
    retry_interval_seconds INT DEFAULT 300,
    timeout_seconds INT DEFAULT 3600,
    priority INT DEFAULT 5,
    run_on_all_tenants BOOLEAN DEFAULT FALSE,
    execution_history_retention_days INT DEFAULT 30,
    is_system_job BOOLEAN DEFAULT FALSE,
    allow_manual_trigger BOOLEAN DEFAULT TRUE,
    created_by UUID,
    last_modified_by UUID,
    last_execution_id UUID,
    next_scheduled_run TIMESTAMPTZ,
    disabled_reason TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_scheduled_job_name CHECK (LENGTH(job_name) >= 3),
    CONSTRAINT ck_scheduled_job_type CHECK (job_type IN ('BACKUP', 'CLEANUP', 'REPORTING', 'SYNC', 'NOTIFICATION', 'MAINTENANCE', 'ANALYTICS', 'IMPORT_EXPORT', 'SYSTEM_HEALTH', 'CUSTOM')),
    CONSTRAINT ck_scheduled_job_schedule CHECK (schedule_type IN ('CRON', 'INTERVAL', 'DAILY', 'WEEKLY', 'MONTHLY', 'ONCE', 'CUSTOM')),
    CONSTRAINT ck_scheduled_job_priority CHECK (priority >= 1 AND priority <= 10)
);

-- JobExecution: Execution history and results of scheduled jobs
CREATE TABLE job_execution (
    execution_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id UUID NOT NULL,
    tenant_id UUID,
    execution_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    execution_start_time TIMESTAMPTZ NOT NULL,
    execution_end_time TIMESTAMPTZ,
    execution_duration_seconds INT,
    total_runtime_seconds INT,
    user_triggered BOOLEAN DEFAULT FALSE,
    triggered_by UUID,
    retry_count INT DEFAULT 0,
    retry_of_execution_id UUID,
    execution_output TEXT,
    execution_result JSONB,
    success BOOLEAN,
    completion_percentage NUMERIC(5,2),
    items_processed INT,
    items_succeeded INT,
    items_failed INT,
    items_skipped INT,
    error_message TEXT,
    error_code VARCHAR(50),
    stack_trace TEXT,
    exit_code INT,
    worker_id VARCHAR(100),
    resource_cpu_percent NUMERIC(5,2),
    resource_memory_mb INT,
    resource_disk_mb INT,
    log_file_path VARCHAR(500),
    execution_node VARCHAR(100),
    next_scheduled_run TIMESTAMPTZ,
    notifications_sent BOOLEAN DEFAULT FALSE,
    archived BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_job_execution_status CHECK (execution_status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED', 'PAUSED', 'TIMEOUT'))
);

-- ErrorLog: System error logging and tracking
CREATE TABLE error_log (
    error_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    error_type VARCHAR(100) NOT NULL,
    error_category VARCHAR(50) NOT NULL,
    error_severity VARCHAR(50) NOT NULL DEFAULT 'ERROR',
    error_code VARCHAR(50),
    error_message TEXT NOT NULL,
    error_detail TEXT,
    stack_trace TEXT,
    source_file VARCHAR(255),
    source_line INT,
    source_function VARCHAR(255),
    user_id UUID,
    session_id VARCHAR(255),
    request_id VARCHAR(255),
    correlation_id VARCHAR(255),
    related_entity_type VARCHAR(100),
    related_entity_id VARCHAR(255),
    http_status_code INT,
    http_method VARCHAR(10),
    http_url VARCHAR(500),
    http_referer VARCHAR(500),
    user_agent TEXT,
    source_ip INET,
    environment VARCHAR(50),
    application_version VARCHAR(50),
    database_version VARCHAR(50),
    is_reported BOOLEAN DEFAULT FALSE,
    is_resolved BOOLEAN DEFAULT FALSE,
    resolved_date TIMESTAMPTZ,
    resolution_notes TEXT,
    resolved_by UUID,
    error_frequency INT DEFAULT 1,
    last_occurrence TIMESTAMPTZ,
    notifications_sent BOOLEAN DEFAULT FALSE,
    ticket_number VARCHAR(100),
    escalation_level INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_error_log_category CHECK (error_category IN ('APPLICATION', 'DATABASE', 'INTEGRATION', 'SECURITY', 'NETWORK', 'INFRASTRUCTURE', 'VALIDATION', 'UNKNOWN')),
    CONSTRAINT ck_error_log_severity CHECK (error_severity IN ('DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL', 'FATAL'))
);

-- FeatureFlag: Feature toggle management
CREATE TABLE feature_flag (
    flag_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    flag_name VARCHAR(255) NOT NULL,
    flag_description TEXT,
    flag_type VARCHAR(50) NOT NULL,
    flag_status VARCHAR(50) NOT NULL DEFAULT 'DISABLED',
    feature_module VARCHAR(100),
    is_beta BOOLEAN DEFAULT FALSE,
    rollout_percentage INT DEFAULT 0,
    enabled_for_users JSONB,
    enabled_for_groups JSONB,
    enabled_for_organizations JSONB,
    enabled_for_environments JSONB,
    condition_rules JSONB,
    default_value BOOLEAN DEFAULT FALSE,
    created_by UUID,
    created_date TIMESTAMPTZ NOT NULL,
    modified_by UUID,
    modified_date TIMESTAMPTZ,
    activation_date TIMESTAMPTZ,
    deactivation_date TIMESTAMPTZ,
    sunset_date DATE,
    deprecation_notice TEXT,
    analytics_enabled BOOLEAN DEFAULT TRUE,
    analytics_data JSONB,
    notification_on_change BOOLEAN DEFAULT FALSE,
    related_feature_flags JSONB,
    release_notes_url VARCHAR(500),
    documentation_url VARCHAR(500),
    is_global_flag BOOLEAN DEFAULT FALSE,
    flag_owner_id UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_feature_flag_name CHECK (LENGTH(flag_name) >= 3),
    CONSTRAINT ck_feature_flag_type CHECK (flag_type IN ('BOOLEAN', 'PERCENTAGE', 'TARGETING', 'SCHEDULED', 'CONDITIONAL')),
    CONSTRAINT ck_feature_flag_status CHECK (flag_status IN ('DISABLED', 'ROLLOUT', 'ENABLED', 'SCHEDULED', 'SUNSETTING', 'DEPRECATED')),
    CONSTRAINT ck_feature_flag_rollout CHECK (rollout_percentage >= 0 AND rollout_percentage <= 100)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_system_config_tenant ON system_config(tenant_id);
CREATE INDEX idx_system_config_category ON system_config(config_category);
CREATE INDEX idx_system_config_key ON system_config(config_key);
CREATE INDEX idx_system_config_is_active ON system_config(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_system_config_modified_by ON system_config(modified_by);
CREATE INDEX idx_scheduled_job_tenant ON scheduled_job(tenant_id);
CREATE INDEX idx_scheduled_job_type ON scheduled_job(job_type);
CREATE INDEX idx_scheduled_job_is_active ON scheduled_job(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_scheduled_job_next_run ON scheduled_job(next_scheduled_run);
CREATE INDEX idx_scheduled_job_is_system ON scheduled_job(is_system_job) WHERE is_system_job = TRUE;
CREATE INDEX idx_job_execution_job ON job_execution(job_id);
CREATE INDEX idx_job_execution_tenant ON job_execution(tenant_id);
CREATE INDEX idx_job_execution_status ON job_execution(execution_status);
CREATE INDEX idx_job_execution_start_time ON job_execution(execution_start_time DESC);
CREATE INDEX idx_job_execution_success ON job_execution(success);
CREATE INDEX idx_job_execution_triggered_by ON job_execution(triggered_by);
CREATE INDEX idx_error_log_tenant ON error_log(tenant_id);
CREATE INDEX idx_error_log_type ON error_log(error_type);
CREATE INDEX idx_error_log_category ON error_log(error_category);
CREATE INDEX idx_error_log_severity ON error_log(error_severity);
CREATE INDEX idx_error_log_date_created ON error_log(date_created DESC);
CREATE INDEX idx_error_log_is_resolved ON error_log(is_resolved) WHERE is_resolved = FALSE;
CREATE INDEX idx_error_log_user ON error_log(user_id);
CREATE INDEX idx_feature_flag_tenant ON feature_flag(tenant_id);
CREATE INDEX idx_feature_flag_name ON feature_flag(flag_name);
CREATE INDEX idx_feature_flag_status ON feature_flag(flag_status);
CREATE INDEX idx_feature_flag_module ON feature_flag(feature_module);
CREATE INDEX idx_feature_flag_is_beta ON feature_flag(is_beta) WHERE is_beta = TRUE;
CREATE INDEX idx_feature_flag_owner ON feature_flag(flag_owner_id);
CREATE INDEX idx_error_log_resolved_by_fk ON error_log(resolved_by);
CREATE INDEX idx_feature_flag_created_by_fk ON feature_flag(created_by);
CREATE INDEX idx_feature_flag_modified_by_fk ON feature_flag(modified_by);
CREATE INDEX idx_job_execution_retry_of_execution_id_fk ON job_execution(retry_of_execution_id);
CREATE INDEX idx_scheduled_job_created_by_fk ON scheduled_job(created_by);
CREATE INDEX idx_scheduled_job_last_modified_by_fk ON scheduled_job(last_modified_by);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_system_config_date_updated BEFORE UPDATE ON system_config FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_scheduled_job_date_updated BEFORE UPDATE ON scheduled_job FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_feature_flag_date_updated BEFORE UPDATE ON feature_flag FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE system_config ENABLE ROW LEVEL SECURITY;
ALTER TABLE system_config FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_system_config_read ON system_config FOR SELECT USING (tenant_id IS NULL OR tenant_id = app_current_tenant_id() OR app_is_platform_admin());
CREATE POLICY rls_system_config_write ON system_config FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE scheduled_job ENABLE ROW LEVEL SECURITY;
ALTER TABLE scheduled_job FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_scheduled_job_read ON scheduled_job FOR SELECT USING (tenant_id IS NULL OR tenant_id = app_current_tenant_id() OR app_is_platform_admin());
CREATE POLICY rls_scheduled_job_write ON scheduled_job FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE job_execution ENABLE ROW LEVEL SECURITY;
ALTER TABLE job_execution FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_job_execution_read ON job_execution FOR SELECT USING (tenant_id IS NULL OR tenant_id = app_current_tenant_id() OR app_is_platform_admin());
CREATE POLICY rls_job_execution_write ON job_execution FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE error_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE error_log FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_error_log_read ON error_log FOR SELECT USING (tenant_id IS NULL OR tenant_id = app_current_tenant_id() OR app_is_platform_admin());
CREATE POLICY rls_error_log_write ON error_log FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE feature_flag ENABLE ROW LEVEL SECURITY;
ALTER TABLE feature_flag FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_feature_flag_read ON feature_flag FOR SELECT USING (tenant_id IS NULL OR tenant_id = app_current_tenant_id() OR app_is_platform_admin());
CREATE POLICY rls_feature_flag_write ON feature_flag FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
