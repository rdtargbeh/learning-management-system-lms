-- ================================================================================
-- 23. FILE MANAGEMENT & STORAGE (7 tables)
-- ================================================================================
-- Tables: file_storage, attachment, file_version, file_folder, storage_quota, accessibility_scan, alternative_format
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- FileStorage: Actual file storage metadata
CREATE TABLE file_storage (
    file_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    folder_id UUID,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    file_size_bytes BIGINT,
    file_hash VARCHAR(64),
    file_url VARCHAR(500) NOT NULL,
    storage_path VARCHAR(500),
    storage_provider VARCHAR(50),
    mime_type VARCHAR(100),
    is_virus_scanned BOOLEAN DEFAULT FALSE,
    is_safe BOOLEAN DEFAULT TRUE,
    uploaded_by UUID,
    access_count INT DEFAULT 0,
    is_public BOOLEAN DEFAULT FALSE,
    usage_right VARCHAR(30),
    license_code VARCHAR(50),
    copyright_holder VARCHAR(255),
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    date_unlock TIMESTAMPTZ,
    date_lock TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_file_storage_file_name CHECK (LENGTH(file_name) >= 3),
    CONSTRAINT ck_file_storage_file_size CHECK (file_size_bytes > 0),
    CONSTRAINT ck_file_storage_usage_right CHECK (usage_right IS NULL OR usage_right IN ('OWN_COPYRIGHT','USED_BY_PERMISSION','PUBLIC_DOMAIN','FAIR_USE','CREATIVE_COMMONS'))
);

-- Attachment: Reference to file_storage from any entity
CREATE TABLE attachment (
    attachment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    file_id UUID NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    attachment_title VARCHAR(255),
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    download_count INT DEFAULT 0,
    display_order INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_attachment_entity_type CHECK (LENGTH(entity_type) >= 3)
);

-- FileVersion: Track file versions
CREATE TABLE file_version (
    version_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    file_id UUID NOT NULL,
    version_number INT NOT NULL,
    version_name VARCHAR(255),
    file_size_bytes BIGINT,
    changed_by UUID,
    change_reason TEXT,
    is_current BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_file_version_number UNIQUE (file_id, version_number)
);

CREATE TABLE file_folder (
    folder_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    parent_folder_id UUID,
    context_type VARCHAR(20) NOT NULL,
    course_id UUID,
    group_id UUID,
    user_id UUID,
    folder_name VARCHAR(255) NOT NULL,
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    date_unlock TIMESTAMPTZ,
    date_lock TIMESTAMPTZ,
    display_order INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_file_folder_context CHECK ( (context_type = 'COURSE' AND course_id IS NOT NULL) OR (context_type = 'GROUP' AND group_id IS NOT NULL) OR (context_type = 'USER' AND user_id IS NOT NULL) OR (context_type = 'TENANT')),
    CONSTRAINT ck_file_folder_context_type CHECK (context_type IN ('COURSE','GROUP','USER','TENANT'))
);

CREATE TABLE storage_quota (
    quota_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    context_type VARCHAR(20) NOT NULL,
    context_id UUID NOT NULL,
    quota_bytes BIGINT NOT NULL,
    used_bytes BIGINT NOT NULL DEFAULT 0,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_storage_quota UNIQUE (context_type, context_id),
    CONSTRAINT ck_storage_quota_context CHECK (context_type IN ('TENANT','ORG_UNIT','COURSE','GROUP','USER')),
    CONSTRAINT ck_storage_quota_values CHECK (quota_bytes >= 0 AND used_bytes >= 0)
);

CREATE TABLE accessibility_scan (
    scan_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID,
    entity_type VARCHAR(30) NOT NULL,
    entity_id UUID NOT NULL,
    file_id UUID,
    accessibility_score NUMERIC(5,2),
    wcag_level VARCHAR(5),
    issues JSONB,
    scan_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    date_scanned TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_accessibility_scan_score CHECK (accessibility_score IS NULL OR accessibility_score BETWEEN 0 AND 100),
    CONSTRAINT ck_accessibility_scan_status CHECK (scan_status IN ('PENDING','COMPLETED','FAILED')),
    CONSTRAINT ck_accessibility_scan_wcag CHECK (wcag_level IS NULL OR wcag_level IN ('A','AA','AAA'))
);

CREATE TABLE alternative_format (
    alternative_format_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    source_file_id UUID NOT NULL,
    format_type VARCHAR(30) NOT NULL,
    output_file_id UUID,
    generation_status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    download_count INT NOT NULL DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_alternative_format UNIQUE (source_file_id, format_type),
    CONSTRAINT ck_alternative_format_type CHECK (format_type IN ('TAGGED_PDF','HTML','EPUB','AUDIO_MP3','ELECTRONIC_BRAILLE','BEELINE_READER','TRANSLATED','OCR_PDF')),
    CONSTRAINT ck_alternative_format_status CHECK (generation_status IN ('QUEUED','GENERATING','READY','FAILED'))
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_file_storage_tenant ON file_storage(tenant_id);
CREATE INDEX idx_file_storage_file_hash ON file_storage(file_hash);
CREATE INDEX idx_file_storage_uploaded_by ON file_storage(uploaded_by);
CREATE INDEX idx_file_storage_date_created ON file_storage(date_created DESC);
CREATE INDEX idx_attachment_file ON attachment(file_id);
CREATE INDEX idx_attachment_entity ON attachment(entity_type, entity_id);
CREATE INDEX idx_attachment_is_visible ON attachment(is_visible);
CREATE INDEX idx_file_version_file ON file_version(file_id);
CREATE INDEX idx_file_version_is_current ON file_version(is_current) WHERE is_current = TRUE;
CREATE INDEX idx_accessibility_scan_entity ON accessibility_scan(entity_type, entity_id);
CREATE INDEX idx_attachment_tenant_id_fk ON attachment(tenant_id);
CREATE INDEX idx_file_version_changed_by_fk ON file_version(changed_by);
CREATE INDEX idx_file_version_tenant_id_fk ON file_version(tenant_id);
CREATE INDEX idx_accessibility_scan_course_id_fk ON accessibility_scan(course_id);
CREATE INDEX idx_accessibility_scan_file_id_fk ON accessibility_scan(file_id);
CREATE INDEX idx_accessibility_scan_tenant_id_fk ON accessibility_scan(tenant_id);
CREATE INDEX idx_alternative_format_output_file_id_fk ON alternative_format(output_file_id);
CREATE INDEX idx_alternative_format_tenant_id_fk ON alternative_format(tenant_id);
CREATE INDEX idx_file_folder_course_id_fk ON file_folder(course_id);
CREATE INDEX idx_file_folder_group_id_fk ON file_folder(group_id);
CREATE INDEX idx_file_folder_parent_folder_id_fk ON file_folder(parent_folder_id);
CREATE INDEX idx_file_folder_tenant_id_fk ON file_folder(tenant_id);
CREATE INDEX idx_file_folder_user_id_fk ON file_folder(user_id);
CREATE INDEX idx_file_storage_folder_id_fk ON file_storage(folder_id);
CREATE INDEX idx_storage_quota_tenant_id_fk ON storage_quota(tenant_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_file_storage_date_updated BEFORE UPDATE ON file_storage FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_file_folder_date_updated BEFORE UPDATE ON file_folder FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_storage_quota_date_updated BEFORE UPDATE ON storage_quota FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE file_storage ENABLE ROW LEVEL SECURITY;
ALTER TABLE file_storage FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_file_storage_tenant ON file_storage FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE attachment ENABLE ROW LEVEL SECURITY;
ALTER TABLE attachment FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_attachment_tenant ON attachment FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE file_version ENABLE ROW LEVEL SECURITY;
ALTER TABLE file_version FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_file_version_tenant ON file_version FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE file_folder ENABLE ROW LEVEL SECURITY;
ALTER TABLE file_folder FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_file_folder_tenant ON file_folder FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE storage_quota ENABLE ROW LEVEL SECURITY;
ALTER TABLE storage_quota FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_storage_quota_tenant ON storage_quota FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE accessibility_scan ENABLE ROW LEVEL SECURITY;
ALTER TABLE accessibility_scan FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_accessibility_scan_tenant ON accessibility_scan FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE alternative_format ENABLE ROW LEVEL SECURITY;
ALTER TABLE alternative_format FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_alternative_format_tenant ON alternative_format FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
