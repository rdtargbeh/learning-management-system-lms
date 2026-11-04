-- ================================================================================
-- 27. AUDIT & LOGGING (3 tables)
-- ================================================================================
-- Tables: activity_log, system_log, compliance_audit
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- ActivityLog: All user actions across the system
CREATE TABLE activity_log (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    activity_type VARCHAR(50) NOT NULL,
    activity_category VARCHAR(50),
    entity_type VARCHAR(100),
    entity_id UUID,
    activity_description TEXT,
    old_value TEXT,
    new_value TEXT,
    ip_address INET,
    user_agent TEXT,
    device_type VARCHAR(50),
    status VARCHAR(50),
    error_message TEXT,
    date_activity TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_activity_log_status CHECK (status IN ('SUCCESS', 'FAILURE', 'WARNING', 'PENDING'))
);

-- SystemLog: System errors and warnings
CREATE TABLE system_log (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    log_level VARCHAR(20),
    log_category VARCHAR(100),
    log_message TEXT,
    error_stack_trace TEXT,
    affected_user_id UUID,
    affected_entity_type VARCHAR(100),
    affected_entity_id UUID,
    source_system VARCHAR(100),
    resolution_status VARCHAR(50),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_system_log_level CHECK (log_level IN ('DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL'))
);

-- ComplianceAudit: FERPA, GDPR, accessibility compliance logging
CREATE TABLE compliance_audit (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID,
    compliance_type VARCHAR(50),
    action_taken VARCHAR(255),
    data_accessed TEXT,
    justification TEXT,
    is_compliant BOOLEAN,
    audit_notes TEXT,
    date_audit TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_activity_log_user ON activity_log(user_id);
CREATE INDEX idx_activity_log_tenant ON activity_log(tenant_id);
CREATE INDEX idx_activity_log_type ON activity_log(activity_type);
CREATE INDEX idx_activity_log_date ON activity_log(date_activity DESC);
CREATE INDEX idx_activity_log_entity ON activity_log(entity_type, entity_id);
CREATE INDEX idx_system_log_tenant ON system_log(tenant_id);
CREATE INDEX idx_system_log_level ON system_log(log_level);
CREATE INDEX idx_system_log_date ON system_log(date_created DESC);
CREATE INDEX idx_compliance_audit_tenant ON compliance_audit(tenant_id);
CREATE INDEX idx_compliance_audit_type ON compliance_audit(compliance_type);
CREATE INDEX idx_compliance_audit_date ON compliance_audit(date_audit DESC);
CREATE INDEX idx_compliance_audit_user_id_fk ON compliance_audit(user_id);
CREATE INDEX idx_system_log_affected_user_id_fk ON system_log(affected_user_id);

-- ---------------------------------------------------------------- TRIGGERS


-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE activity_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE activity_log FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_activity_log_tenant ON activity_log FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE system_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE system_log FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_system_log_tenant ON system_log FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE compliance_audit ENABLE ROW LEVEL SECURITY;
ALTER TABLE compliance_audit FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_compliance_audit_tenant ON compliance_audit FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
