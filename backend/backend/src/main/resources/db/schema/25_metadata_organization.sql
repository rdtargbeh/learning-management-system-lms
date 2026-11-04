-- ================================================================================
-- 25. METADATA & ORGANIZATION (3 tables)
-- ================================================================================
-- Tables: tag, entity_tag, entity_version
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- Tag: Generic tags used across system
CREATE TABLE tag (
    tag_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    tag_name VARCHAR(100) NOT NULL,
    tag_category VARCHAR(50),
    tag_color VARCHAR(7),
    usage_count INT DEFAULT 0,
    is_system_tag BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tag_name_tenant UNIQUE (tag_name, tenant_id),
    CONSTRAINT ck_tag_name CHECK (LENGTH(tag_name) >= 2)
);

-- EntityTag: Bridge table for tagging any entity
CREATE TABLE entity_tag (
    entity_tag_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    tag_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    date_tagged TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_entity_tag UNIQUE (tag_id, entity_type, entity_id),
    CONSTRAINT ck_entity_tag_type CHECK (LENGTH(entity_type) >= 3)
);

-- EntityVersion: Generic versioning for any entity
CREATE TABLE entity_version (
    version_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(100) NOT NULL,
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    entity_id UUID NOT NULL,
    version_number INT NOT NULL,
    version_name VARCHAR(255),
    entity_data JSONB,
    changed_by UUID,
    change_reason TEXT,
    change_type VARCHAR(50),
    is_current BOOLEAN DEFAULT FALSE,
    ip_address INET,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_entity_version UNIQUE (entity_type, entity_id, version_number),
    CONSTRAINT ck_entity_version_type CHECK (LENGTH(entity_type) >= 3)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_tag_tenant ON tag(tenant_id);
CREATE INDEX idx_tag_category ON tag(tag_category);
CREATE INDEX idx_tag_is_system ON tag(is_system_tag);
CREATE INDEX idx_entity_tag_tenant ON entity_tag(tenant_id);
CREATE INDEX idx_entity_tag_tag ON entity_tag(tag_id);
CREATE INDEX idx_entity_tag_entity ON entity_tag(entity_type, entity_id);
CREATE INDEX idx_entity_version_entity ON entity_version(entity_type, entity_id);
CREATE INDEX idx_entity_version_is_current ON entity_version(is_current);
CREATE INDEX idx_entity_version_changed_by_fk ON entity_version(changed_by);
CREATE INDEX idx_entity_version_tenant_id_fk ON entity_version(tenant_id);

-- ---------------------------------------------------------------- TRIGGERS


-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE tag ENABLE ROW LEVEL SECURITY;
ALTER TABLE tag FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_tag_tenant ON tag FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE entity_tag ENABLE ROW LEVEL SECURITY;
ALTER TABLE entity_tag FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_entity_tag_tenant ON entity_tag FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE entity_version ENABLE ROW LEVEL SECURITY;
ALTER TABLE entity_version FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_entity_version_tenant ON entity_version FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
