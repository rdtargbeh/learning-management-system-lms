-- ================================================================================
-- 19. INTEGRATIONS & INTEROPERABILITY (15 tables)
-- ================================================================================
-- Tables: external_tool, lti_consumer, lti_provider, external_tool_usage, content_package, xapi_statement, api_key, webhook_integration, sso_configuration, integration_log, sis_integration, sis_sync_run, lti_registration, lti_resource_link, course_copy_job
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- ExternalTool: External tool/application integrations
CREATE TABLE external_tool (
    tool_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    tool_name VARCHAR(255) NOT NULL,
    tool_description TEXT,
    tool_type VARCHAR(50) NOT NULL,
    tool_provider VARCHAR(100),
    tool_url VARCHAR(500),
    tool_icon_url VARCHAR(500),
    api_endpoint VARCHAR(500),
    authentication_type VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_approved BOOLEAN DEFAULT FALSE,
    approved_date TIMESTAMPTZ,
    approved_by UUID,
    installation_status VARCHAR(50) DEFAULT 'INSTALLED',
    configuration_status VARCHAR(50) DEFAULT 'CONFIGURED',
    enabled_features JSONB,
    required_scopes JSONB,
    rate_limit_calls INT,
    rate_limit_period VARCHAR(50),
    support_contact VARCHAR(255),
    documentation_url VARCHAR(500),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_external_tool_name CHECK (LENGTH(tool_name) >= 3),
    CONSTRAINT ck_external_tool_type CHECK (tool_type IN ('LTI', 'API_INTEGRATION', 'SSO_PROVIDER', 'CONTENT_REPOSITORY', 'ASSESSMENT_TOOL', 'ANALYTICS', 'COMMUNICATION', 'CUSTOM')),
    CONSTRAINT ck_external_tool_auth CHECK (authentication_type IN ('OAUTH2', 'API_KEY', 'BASIC_AUTH', 'LTI_SIGNATURE', 'NONE')),
    CONSTRAINT ck_external_tool_status CHECK (installation_status IN ('INSTALLED', 'INSTALLING', 'UNINSTALLING', 'FAILED')),
    CONSTRAINT ck_external_tool_config CHECK (configuration_status IN ('CONFIGURED', 'PENDING_CONFIG', 'NEEDS_REVIEW', 'ERROR'))
);

-- LTIConsumer: LTI consumer configuration (LMS as consumer)
CREATE TABLE lti_consumer (
    consumer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    external_tool_id UUID NOT NULL,
    consumer_key VARCHAR(255) NOT NULL UNIQUE,
    consumer_secret VARCHAR(500) NOT NULL,
    consumer_version VARCHAR(50) NOT NULL,
    consumer_name VARCHAR(255),
    consumer_guid VARCHAR(255),
    launch_url VARCHAR(500) NOT NULL,
    content_item_selection_url VARCHAR(500),
    tool_consumer_info_url VARCHAR(500),
    icon_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    oauth_token_endpoint VARCHAR(500),
    oauth_authorization_endpoint VARCHAR(500),
    oauth_client_id VARCHAR(255),
    oauth_client_secret VARCHAR(500),
    supported_lti_versions JSONB,
    launch_privacy_level VARCHAR(50) DEFAULT 'PUBLIC',
    enable_tool_state BOOLEAN DEFAULT FALSE,
    domain_whitelist JSONB,
    custom_parameters JSONB,
    last_launch TIMESTAMPTZ,
    launch_count INT DEFAULT 0,
    error_count INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_lti_consumer_version CHECK (consumer_version IN ('1.0', '1.1', '1.3', '2.0')),
    CONSTRAINT ck_lti_consumer_privacy CHECK (launch_privacy_level IN ('PUBLIC', 'PRIVATE', 'RESTRICTED'))
);

-- LTIProvider: LTI provider configuration (LMS as provider)
CREATE TABLE lti_provider (
    provider_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    provider_name VARCHAR(255) NOT NULL,
    provider_description TEXT,
    provider_key VARCHAR(255) NOT NULL UNIQUE,
    provider_secret VARCHAR(500) NOT NULL,
    provider_version VARCHAR(50) NOT NULL,
    provider_url VARCHAR(500),
    provider_icon_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    allow_account_creation BOOLEAN DEFAULT FALSE,
    auto_activate_accounts BOOLEAN DEFAULT FALSE,
    supported_extensions JSONB,
    grade_passback_enabled BOOLEAN DEFAULT FALSE,
    outcome_service_url VARCHAR(500),
    result_sourceid_prefix VARCHAR(50),
    unique_sourceid_per_context BOOLEAN DEFAULT TRUE,
    max_active_contexts INT,
    current_active_contexts INT DEFAULT 0,
    ip_whitelist JSONB,
    oauth_settings JSONB,
    custom_parameters JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_lti_provider_name CHECK (LENGTH(provider_name) >= 3),
    CONSTRAINT ck_lti_provider_version CHECK (provider_version IN ('1.0', '1.1', '1.3', '2.0'))
);

-- ExternalToolUsage: Track usage of external tools
CREATE TABLE external_tool_usage (
    usage_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    tool_id UUID NOT NULL,
    course_id UUID,
    section_id UUID,
    user_id UUID,
    content_id UUID,
    assignment_id UUID,
    assessment_id UUID,
    usage_type VARCHAR(50) NOT NULL,
    usage_context VARCHAR(50),
    launch_count INT DEFAULT 1,
    total_time_minutes INT DEFAULT 0,
    first_used TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    usage_success_rate NUMERIC(5,2),
    error_message TEXT,
    user_feedback_rating NUMERIC(3,1),
    user_feedback_text TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_external_tool_usage_type CHECK (usage_type IN ('CONTENT_EMBED', 'ASSIGNMENT_SUBMISSION', 'ASSESSMENT_TOOL', 'GRADING_TOOL', 'ANALYTICS', 'COMMUNICATION', 'COURSE_TOOL')),
    CONSTRAINT ck_external_tool_usage_rate CHECK (usage_success_rate IS NULL OR (usage_success_rate >= 0 AND usage_success_rate <= 100))
);

-- ContentPackage: IMS-CC content package support
CREATE TABLE content_package (
    package_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID,
    package_name VARCHAR(255) NOT NULL,
    package_description TEXT,
    package_type VARCHAR(50) NOT NULL,
    package_file_path VARCHAR(500),
    package_file_size INT,
    package_format VARCHAR(50),
    package_version VARCHAR(50),
    package_status VARCHAR(50) NOT NULL DEFAULT 'UPLOADED',
    import_status VARCHAR(50),
    import_progress NUMERIC(5,2),
    imported_items_count INT DEFAULT 0,
    import_errors INT DEFAULT 0,
    import_warnings INT DEFAULT 0,
    import_date TIMESTAMPTZ,
    imported_by UUID,
    manifest_data JSONB,
    metadata JSONB,
    is_archived BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_content_package_name CHECK (LENGTH(package_name) >= 3),
    CONSTRAINT ck_content_package_type CHECK (package_type IN ('IMS_CC', 'SCORM', 'XAPI', 'QTI', 'CANVAS_EXPORT', 'MOODLE_EXPORT', 'BLACKBOARD_EXPORT')),
    CONSTRAINT ck_content_package_status CHECK (package_status IN ('UPLOADED', 'VALIDATING', 'VALID', 'INVALID', 'IMPORTING', 'IMPORTED', 'FAILED')),
    CONSTRAINT ck_content_package_import CHECK (import_status IS NULL OR import_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED'))
);

-- xAPIStatement: Experience API (xAPI) learning record store
CREATE TABLE xapi_statement (
    statement_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    actor_id UUID NOT NULL,
    actor_mbox VARCHAR(255),
    actor_account_name VARCHAR(255),
    verb VARCHAR(100) NOT NULL,
    verb_display VARCHAR(255),
    object_type VARCHAR(50) NOT NULL,
    object_id VARCHAR(500),
    object_definition_name VARCHAR(255),
    object_definition_description TEXT,
    result_completion BOOLEAN,
    result_success BOOLEAN,
    result_score NUMERIC(5,2),
    result_response TEXT,
    result_duration VARCHAR(20),
    context_course_id UUID,
    context_session_id UUID,
    context_activity_id VARCHAR(500),
    context_language VARCHAR(10),
    timestamp TIMESTAMPTZ NOT NULL,
    stored TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version VARCHAR(50),
    statement_data JSONB,
    is_voided BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_xapi_statement_verb CHECK (LENGTH(verb) >= 3),
    CONSTRAINT ck_xapi_statement_object_type CHECK (object_type IN ('ACTIVITY', 'AGENT', 'GROUP', 'STATEMENT'))
);

-- APIKey: API key management for external integrations
CREATE TABLE api_key (
    key_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    created_by UUID NOT NULL,
    key_name VARCHAR(255) NOT NULL,
    key_description TEXT,
    api_key_hash VARCHAR(255) NOT NULL UNIQUE,
    api_secret_hash VARCHAR(255),
    key_type VARCHAR(50) NOT NULL,
    scopes JSONB,
    rate_limit_calls INT,
    rate_limit_period VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_revoked BOOLEAN DEFAULT FALSE,
    revoked_date TIMESTAMPTZ,
    revoked_reason TEXT,
    last_used TIMESTAMPTZ,
    ip_whitelist JSONB,
    allowed_endpoints JSONB,
    usage_count INT DEFAULT 0,
    error_count INT DEFAULT 0,
    expiration_date DATE,
    notification_email VARCHAR(255),
    notify_expiration_days INT DEFAULT 7,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_api_key_name CHECK (LENGTH(key_name) >= 3),
    CONSTRAINT ck_api_key_type CHECK (key_type IN ('INTEGRATION', 'DEVELOPER', 'PARTNER', 'INTERNAL', 'SERVICE_ACCOUNT')),
    CONSTRAINT ck_api_key_active CHECK (is_revoked = FALSE OR is_active = FALSE)
);

-- WebhookIntegration: Webhook configuration for push notifications
CREATE TABLE webhook_integration (
    webhook_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    webhook_url VARCHAR(500) NOT NULL,
    webhook_name VARCHAR(255),
    webhook_description TEXT,
    event_types JSONB NOT NULL,
    webhook_secret VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    retry_enabled BOOLEAN DEFAULT TRUE,
    retry_max_attempts INT DEFAULT 5,
    retry_backoff_seconds INT DEFAULT 60,
    timeout_seconds INT DEFAULT 30,
    delivery_status VARCHAR(50) DEFAULT 'ACTIVE',
    last_delivery TIMESTAMPTZ,
    last_delivery_status VARCHAR(50),
    successful_deliveries INT DEFAULT 0,
    failed_deliveries INT DEFAULT 0,
    pending_deliveries INT DEFAULT 0,
    ssl_verify BOOLEAN DEFAULT TRUE,
    custom_headers JSONB,
    request_template VARCHAR(50) DEFAULT 'JSON',
    batching_enabled BOOLEAN DEFAULT FALSE,
    batch_size INT,
    batch_timeout_seconds INT,
    rate_limit_calls INT,
    rate_limit_period VARCHAR(50),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_webhook_integration_url CHECK (webhook_url LIKE 'https://%'),
    CONSTRAINT ck_webhook_integration_status CHECK (delivery_status IN ('ACTIVE', 'SUSPENDED', 'DISABLED', 'ERROR')),
    CONSTRAINT ck_webhook_integration_template CHECK (request_template IN ('JSON', 'FORM_ENCODED', 'XML'))
);

-- SSOConfiguration: Single Sign-On (SSO) provider configuration
CREATE TABLE sso_configuration (
    config_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE DEFAULT app_current_tenant_id(),
    sso_provider VARCHAR(50) NOT NULL,
    provider_name VARCHAR(255),
    is_primary_sso BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    auto_provision_users BOOLEAN DEFAULT TRUE,
    auto_activate_accounts BOOLEAN DEFAULT FALSE,
    just_in_time_provisioning BOOLEAN DEFAULT TRUE,
    sync_profile_data BOOLEAN DEFAULT TRUE,
    sync_user_attributes JSONB,
    create_unknown_users BOOLEAN DEFAULT FALSE,
    update_user_email BOOLEAN DEFAULT TRUE,
    case_sensitive_username BOOLEAN DEFAULT FALSE,
    username_normalization VARCHAR(50),
    default_user_role VARCHAR(100),
    configuration_data JSONB NOT NULL,
    certificate_url VARCHAR(500),
    certificate_thumbprint VARCHAR(255),
    encryption_enabled BOOLEAN DEFAULT TRUE,
    signing_enabled BOOLEAN DEFAULT TRUE,
    metadata_url VARCHAR(500),
    entity_id VARCHAR(500),
    acs_url VARCHAR(500),
    sso_login_url VARCHAR(500),
    sso_logout_url VARCHAR(500),
    idp_issuer VARCHAR(255),
    assertion_consumer_service_url VARCHAR(500),
    single_logout_service_url VARCHAR(500),
    nameid_format VARCHAR(100),
    date_tested TIMESTAMPTZ,
    last_sync TIMESTAMPTZ,
    sync_count INT DEFAULT 0,
    sync_error_count INT DEFAULT 0,
    last_error_message TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_sso_configuration_provider CHECK (sso_provider IN ('SAML', 'OAUTH2', 'OPENID_CONNECT', 'AZURE_AD', 'GOOGLE', 'OKTA', 'SHIBBOLETH', 'CAS', 'LDAP'))
);

-- IntegrationLog: Audit trail for all integrations
CREATE TABLE integration_log (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    integration_type VARCHAR(50) NOT NULL,
    tool_id UUID,
    api_key_id UUID,
    webhook_id UUID,
    sso_config_id UUID,
    log_action VARCHAR(50) NOT NULL,
    source_system VARCHAR(100),
    target_system VARCHAR(100),
    request_data JSONB,
    response_data JSONB,
    status_code INT,
    error_message TEXT,
    error_stack_trace TEXT,
    latency_ms INT,
    processed_records INT,
    failed_records INT,
    performed_by UUID,
    ip_address INET,
    user_agent TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_integration_log_type CHECK (integration_type IN ('LTI', 'API', 'WEBHOOK', 'SSO', 'CONTENT_IMPORT', 'XAPI', 'EXTERNAL_TOOL')),
    CONSTRAINT ck_integration_log_action CHECK (log_action IN ('INITIALIZE', 'AUTHENTICATE', 'FETCH_DATA', 'SEND_DATA', 'SYNC', 'ERROR', 'WEBHOOK_DELIVERY', 'USER_LOGIN'))
);

-- SIS INTEGRATION: roster/term/course/grade sync with Banner, PeopleSoft, Workday, Colleague (OneRoster / CSV)
CREATE TABLE sis_integration (
    sis_integration_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    provider VARCHAR(50) NOT NULL,
    integration_name VARCHAR(255) NOT NULL,
    protocol VARCHAR(30) NOT NULL DEFAULT 'ONEROSTER_REST',
    base_url VARCHAR(1000),
    credentials_secret_ref VARCHAR(255),
    sync_direction VARCHAR(20) NOT NULL DEFAULT 'INBOUND',
    sync_schedule VARCHAR(100),
    grade_passback_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    config JSONB,
    date_last_sync TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_sis_integration_provider CHECK (provider IN ('BANNER','PEOPLESOFT','WORKDAY','COLLEAGUE','JENZABAR','ANTHOLOGY','CAMPUS_NEXUS','CUSTOM')),
    CONSTRAINT ck_sis_integration_protocol CHECK (protocol IN ('ONEROSTER_REST','ONEROSTER_CSV','LIS','CSV','API')),
    CONSTRAINT ck_sis_integration_direction CHECK (sync_direction IN ('INBOUND','OUTBOUND','BIDIRECTIONAL'))
);

CREATE TABLE sis_sync_run (
    sync_run_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    sis_integration_id UUID NOT NULL,
    sync_type VARCHAR(30) NOT NULL,
    run_status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    source_file_id UUID,
    is_diff_mode BOOLEAN NOT NULL DEFAULT TRUE,
    records_processed INT NOT NULL DEFAULT 0,
    records_created INT NOT NULL DEFAULT 0,
    records_updated INT NOT NULL DEFAULT 0,
    records_deleted INT NOT NULL DEFAULT 0,
    records_failed INT NOT NULL DEFAULT 0,
    error_report JSONB,
    date_start TIMESTAMPTZ,
    date_end TIMESTAMPTZ,
    triggered_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_sis_sync_run_type CHECK (sync_type IN ('FULL','USERS','TERMS','COURSES','SECTIONS','ENROLLMENTS','GRADES_OUT')),
    CONSTRAINT ck_sis_sync_run_status CHECK (run_status IN ('QUEUED','RUNNING','COMPLETED','COMPLETED_WITH_ERRORS','FAILED','CANCELLED'))
);

-- LTI 1.3 / LTI Advantage (the existing lti_consumer/lti_provider tables are LTI 1.1 key/secret style)
CREATE TABLE lti_registration (
    registration_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    external_tool_id UUID NOT NULL,
    lti_version VARCHAR(10) NOT NULL DEFAULT '1.3',
    issuer VARCHAR(500) NOT NULL,
    client_id VARCHAR(255) NOT NULL,
    deployment_id VARCHAR(255) NOT NULL,
    oidc_login_url VARCHAR(1000) NOT NULL,
    target_link_uri VARCHAR(1000) NOT NULL,
    redirect_uris JSONB,
    tool_jwks_url VARCHAR(1000),
    tool_public_jwk JSONB,
    platform_key_id VARCHAR(255),
    scopes JSONB,
    ags_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    nrps_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    deep_linking_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    placements JSONB,
    privacy_level VARCHAR(20) NOT NULL DEFAULT 'ANONYMOUS',
    custom_parameters JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_lti_registration_client UNIQUE (tenant_id, client_id, deployment_id),
    CONSTRAINT ck_lti_registration_version CHECK (lti_version IN ('1.1','1.3')),
    CONSTRAINT ck_lti_registration_privacy CHECK (privacy_level IN ('ANONYMOUS','NAME_ONLY','EMAIL_ONLY','PUBLIC'))
);

CREATE TABLE lti_resource_link (
    resource_link_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    registration_id UUID NOT NULL,
    course_id UUID NOT NULL,
    content_id UUID,
    assignment_id UUID,
    grade_item_id UUID,
    lti_resource_link_ref VARCHAR(255) NOT NULL,
    link_title VARCHAR(255),
    launch_url VARCHAR(1000),
    custom_parameters JSONB,
    ags_line_item_url VARCHAR(1000),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_lti_resource_link_ref UNIQUE (registration_id, lti_resource_link_ref)
);

-- COURSE COPY (content_package covers imports of Common Cartridge / SCORM / QTI; this covers course-to-course copy)
CREATE TABLE course_copy_job (
    copy_job_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    source_course_id UUID NOT NULL,
    target_course_id UUID NOT NULL,
    copy_options JSONB,
    shift_dates BOOLEAN NOT NULL DEFAULT FALSE,
    date_shift_days INT,
    job_status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    progress_percent INT NOT NULL DEFAULT 0,
    job_log JSONB,
    requested_by UUID,
    date_start TIMESTAMPTZ,
    date_end TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_course_copy_job_status CHECK (job_status IN ('QUEUED','RUNNING','COMPLETED','FAILED','CANCELLED')),
    CONSTRAINT ck_course_copy_job_progress CHECK (progress_percent BETWEEN 0 AND 100),
    CONSTRAINT ck_course_copy_job_not_self CHECK (source_course_id <> target_course_id)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_external_tool_tenant ON external_tool(tenant_id);
CREATE INDEX idx_external_tool_type ON external_tool(tool_type);
CREATE INDEX idx_external_tool_is_active ON external_tool(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_external_tool_is_approved ON external_tool(is_approved) WHERE is_approved = TRUE;
CREATE INDEX idx_lti_consumer_external_tool ON lti_consumer(external_tool_id);
CREATE INDEX idx_lti_consumer_is_active ON lti_consumer(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_lti_consumer_last_launch ON lti_consumer(last_launch DESC);
CREATE INDEX idx_lti_provider_tenant ON lti_provider(tenant_id);
CREATE INDEX idx_lti_provider_is_active ON lti_provider(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_external_tool_usage_tool ON external_tool_usage(tool_id);
CREATE INDEX idx_external_tool_usage_course ON external_tool_usage(course_id);
CREATE INDEX idx_external_tool_usage_user ON external_tool_usage(user_id);
CREATE INDEX idx_external_tool_usage_last_used ON external_tool_usage(last_used DESC);
CREATE INDEX idx_content_package_tenant ON content_package(tenant_id);
CREATE INDEX idx_content_package_course ON content_package(course_id);
CREATE INDEX idx_content_package_status ON content_package(package_status);
CREATE INDEX idx_content_package_import_status ON content_package(import_status);
CREATE INDEX idx_xapi_statement_tenant ON xapi_statement(tenant_id);
CREATE INDEX idx_xapi_statement_actor ON xapi_statement(actor_id);
CREATE INDEX idx_xapi_statement_course ON xapi_statement(context_course_id);
CREATE INDEX idx_xapi_statement_verb ON xapi_statement(verb);
CREATE INDEX idx_xapi_statement_timestamp ON xapi_statement(timestamp DESC);
CREATE INDEX idx_api_key_tenant ON api_key(tenant_id);
CREATE INDEX idx_api_key_created_by ON api_key(created_by);
CREATE INDEX idx_api_key_is_active ON api_key(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_api_key_is_revoked ON api_key(is_revoked) WHERE is_revoked = FALSE;
CREATE INDEX idx_api_key_expiration ON api_key(expiration_date);
CREATE INDEX idx_webhook_integration_tenant ON webhook_integration(tenant_id);
CREATE INDEX idx_webhook_integration_is_active ON webhook_integration(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_webhook_integration_status ON webhook_integration(delivery_status);
CREATE INDEX idx_sso_configuration_provider ON sso_configuration(sso_provider);
CREATE INDEX idx_sso_configuration_is_active ON sso_configuration(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_integration_log_tenant ON integration_log(tenant_id);
CREATE INDEX idx_integration_log_type ON integration_log(integration_type);
CREATE INDEX idx_integration_log_status ON integration_log(status_code);
CREATE INDEX idx_integration_log_date ON integration_log(date_created DESC);
CREATE INDEX idx_integration_log_tool ON integration_log(tool_id);
CREATE INDEX idx_lti_consumer_tenant ON lti_consumer(tenant_id);
CREATE INDEX idx_external_tool_usage_tenant ON external_tool_usage(tenant_id);
CREATE INDEX idx_sis_sync_run_integration ON sis_sync_run(sis_integration_id, date_created DESC);
CREATE INDEX idx_content_package_imported_by_fk ON content_package(imported_by);
CREATE INDEX idx_course_copy_job_requested_by_fk ON course_copy_job(requested_by);
CREATE INDEX idx_course_copy_job_source_course_id_fk ON course_copy_job(source_course_id);
CREATE INDEX idx_course_copy_job_target_course_id_fk ON course_copy_job(target_course_id);
CREATE INDEX idx_course_copy_job_tenant_id_fk ON course_copy_job(tenant_id);
CREATE INDEX idx_external_tool_approved_by_fk ON external_tool(approved_by);
CREATE INDEX idx_external_tool_usage_assessment_id_fk ON external_tool_usage(assessment_id);
CREATE INDEX idx_external_tool_usage_assignment_id_fk ON external_tool_usage(assignment_id);
CREATE INDEX idx_external_tool_usage_content_id_fk ON external_tool_usage(content_id);
CREATE INDEX idx_external_tool_usage_section_id_fk ON external_tool_usage(section_id);
CREATE INDEX idx_integration_log_api_key_id_fk ON integration_log(api_key_id);
CREATE INDEX idx_integration_log_performed_by_fk ON integration_log(performed_by);
CREATE INDEX idx_integration_log_sso_config_id_fk ON integration_log(sso_config_id);
CREATE INDEX idx_integration_log_webhook_id_fk ON integration_log(webhook_id);
CREATE INDEX idx_lti_registration_external_tool_id_fk ON lti_registration(external_tool_id);
CREATE INDEX idx_lti_resource_link_assignment_id_fk ON lti_resource_link(assignment_id);
CREATE INDEX idx_lti_resource_link_content_id_fk ON lti_resource_link(content_id);
CREATE INDEX idx_lti_resource_link_course_id_fk ON lti_resource_link(course_id);
CREATE INDEX idx_lti_resource_link_grade_item_id_fk ON lti_resource_link(grade_item_id);
CREATE INDEX idx_lti_resource_link_tenant_id_fk ON lti_resource_link(tenant_id);
CREATE INDEX idx_sis_integration_tenant_id_fk ON sis_integration(tenant_id);
CREATE INDEX idx_sis_sync_run_source_file_id_fk ON sis_sync_run(source_file_id);
CREATE INDEX idx_sis_sync_run_tenant_id_fk ON sis_sync_run(tenant_id);
CREATE INDEX idx_sis_sync_run_triggered_by_fk ON sis_sync_run(triggered_by);
CREATE INDEX idx_xapi_statement_context_session_id_fk ON xapi_statement(context_session_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_external_tool_date_updated BEFORE UPDATE ON external_tool FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_lti_consumer_date_updated BEFORE UPDATE ON lti_consumer FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_lti_provider_date_updated BEFORE UPDATE ON lti_provider FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_content_package_date_updated BEFORE UPDATE ON content_package FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_api_key_date_updated BEFORE UPDATE ON api_key FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_webhook_integration_date_updated BEFORE UPDATE ON webhook_integration FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_sso_configuration_date_updated BEFORE UPDATE ON sso_configuration FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_sis_integration_date_updated BEFORE UPDATE ON sis_integration FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_lti_registration_date_updated BEFORE UPDATE ON lti_registration FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_lti_resource_link_date_updated BEFORE UPDATE ON lti_resource_link FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_course_copy_job_date_updated BEFORE UPDATE ON course_copy_job FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE external_tool ENABLE ROW LEVEL SECURITY;
ALTER TABLE external_tool FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_external_tool_tenant ON external_tool FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE lti_consumer ENABLE ROW LEVEL SECURITY;
ALTER TABLE lti_consumer FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_lti_consumer_tenant ON lti_consumer FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE lti_provider ENABLE ROW LEVEL SECURITY;
ALTER TABLE lti_provider FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_lti_provider_tenant ON lti_provider FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE external_tool_usage ENABLE ROW LEVEL SECURITY;
ALTER TABLE external_tool_usage FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_external_tool_usage_tenant ON external_tool_usage FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE content_package ENABLE ROW LEVEL SECURITY;
ALTER TABLE content_package FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_content_package_tenant ON content_package FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE xapi_statement ENABLE ROW LEVEL SECURITY;
ALTER TABLE xapi_statement FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_xapi_statement_tenant ON xapi_statement FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE api_key ENABLE ROW LEVEL SECURITY;
ALTER TABLE api_key FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_api_key_tenant ON api_key FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE webhook_integration ENABLE ROW LEVEL SECURITY;
ALTER TABLE webhook_integration FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_webhook_integration_tenant ON webhook_integration FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE sso_configuration ENABLE ROW LEVEL SECURITY;
ALTER TABLE sso_configuration FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_sso_configuration_tenant ON sso_configuration FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE integration_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE integration_log FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_integration_log_tenant ON integration_log FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE sis_integration ENABLE ROW LEVEL SECURITY;
ALTER TABLE sis_integration FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_sis_integration_tenant ON sis_integration FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE sis_sync_run ENABLE ROW LEVEL SECURITY;
ALTER TABLE sis_sync_run FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_sis_sync_run_tenant ON sis_sync_run FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE lti_registration ENABLE ROW LEVEL SECURITY;
ALTER TABLE lti_registration FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_lti_registration_tenant ON lti_registration FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE lti_resource_link ENABLE ROW LEVEL SECURITY;
ALTER TABLE lti_resource_link FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_lti_resource_link_tenant ON lti_resource_link FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course_copy_job ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_copy_job FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_copy_job_tenant ON course_copy_job FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
