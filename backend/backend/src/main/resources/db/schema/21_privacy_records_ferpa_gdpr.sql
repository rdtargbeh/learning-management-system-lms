-- ================================================================================
-- 21. PRIVACY & RECORDS (FERPA / GDPR) (4 tables)
-- ================================================================================
-- Tables: consent_record, data_request, retention_policy, legal_hold
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- PRIVACY & RECORDS (FERPA / GDPR): consent, subject requests, retention, legal hold
CREATE TABLE consent_record (
    consent_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    policy_id UUID,
    consent_type VARCHAR(50) NOT NULL,
    consent_version VARCHAR(50) NOT NULL,
    is_granted BOOLEAN NOT NULL,
    date_granted TIMESTAMPTZ,
    date_revoked TIMESTAMPTZ,
    ip_address INET,
    user_agent TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_consent_record_type CHECK (consent_type IN ('TERMS_OF_USE','PRIVACY_POLICY','FERPA_DIRECTORY_RELEASE','COOKIES','RECORDING','ANALYTICS','MARKETING','THIRD_PARTY_TOOL'))
);

CREATE TABLE data_request (
    data_request_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    subject_user_id UUID NOT NULL,
    requested_by UUID,
    request_type VARCHAR(30) NOT NULL,
    request_status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    legal_basis VARCHAR(100),
    request_details TEXT,
    export_file_id UUID,
    date_due DATE,
    date_completed TIMESTAMPTZ,
    handled_by UUID,
    resolution_notes TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_data_request_type CHECK (request_type IN ('ACCESS','EXPORT','RECTIFICATION','ERASURE','RESTRICTION','FERPA_INSPECTION','FERPA_AMENDMENT')),
    CONSTRAINT ck_data_request_status CHECK (request_status IN ('RECEIVED','VERIFYING','IN_PROGRESS','COMPLETED','REJECTED','ON_HOLD'))
);

CREATE TABLE retention_policy (
    retention_policy_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    entity_type VARCHAR(100) NOT NULL,
    retention_days INT NOT NULL,
    action_on_expiry VARCHAR(20) NOT NULL DEFAULT 'ARCHIVE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_last_enforced TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_retention_policy_entity UNIQUE (tenant_id, entity_type),
    CONSTRAINT ck_retention_policy_days CHECK (retention_days > 0),
    CONSTRAINT ck_retention_policy_action CHECK (action_on_expiry IN ('ARCHIVE','ANONYMIZE','DELETE'))
);

CREATE TABLE legal_hold (
    legal_hold_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    hold_reason TEXT NOT NULL,
    case_reference VARCHAR(255),
    imposed_by UUID,
    date_start TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_end TIMESTAMPTZ,
    released_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_legal_hold_dates CHECK (date_end IS NULL OR date_end >= date_start)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_consent_record_user ON consent_record(user_id, consent_type);
CREATE INDEX idx_legal_hold_entity ON legal_hold(entity_type, entity_id) WHERE date_end IS NULL;
CREATE INDEX idx_consent_record_policy_id_fk ON consent_record(policy_id);
CREATE INDEX idx_consent_record_tenant_id_fk ON consent_record(tenant_id);
CREATE INDEX idx_data_request_export_file_id_fk ON data_request(export_file_id);
CREATE INDEX idx_data_request_handled_by_fk ON data_request(handled_by);
CREATE INDEX idx_data_request_requested_by_fk ON data_request(requested_by);
CREATE INDEX idx_data_request_subject_user_id_fk ON data_request(subject_user_id);
CREATE INDEX idx_data_request_tenant_id_fk ON data_request(tenant_id);
CREATE INDEX idx_legal_hold_imposed_by_fk ON legal_hold(imposed_by);
CREATE INDEX idx_legal_hold_released_by_fk ON legal_hold(released_by);
CREATE INDEX idx_legal_hold_tenant_id_fk ON legal_hold(tenant_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_data_request_date_updated BEFORE UPDATE ON data_request FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_retention_policy_date_updated BEFORE UPDATE ON retention_policy FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE consent_record ENABLE ROW LEVEL SECURITY;
ALTER TABLE consent_record FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_consent_record_tenant ON consent_record FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE data_request ENABLE ROW LEVEL SECURITY;
ALTER TABLE data_request FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_data_request_tenant ON data_request FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE retention_policy ENABLE ROW LEVEL SECURITY;
ALTER TABLE retention_policy FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_retention_policy_tenant ON retention_policy FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE legal_hold ENABLE ROW LEVEL SECURITY;
ALTER TABLE legal_hold FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_legal_hold_tenant ON legal_hold FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
