-- ================================================================================
-- 01. TENANCY & INSTITUTION MANAGEMENT (7 tables)
-- ================================================================================
-- Tables: tenant, institution, tenant_settings, tenant_branding, tenant_domain, tenant_subscription, tenant_user_limit
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- Tenant: Multi-tenant system instance (PARENT)
CREATE TABLE tenant (
    tenant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_name VARCHAR(255) NOT NULL,
    tenant_code VARCHAR(50) UNIQUE,
    tenant_description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_status VARCHAR(20) DEFAULT 'ACTIVE',
    data_region VARCHAR(50),
    default_timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    default_locale VARCHAR(10) NOT NULL DEFAULT 'en',
    metadata JSONB,
    created_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    suspended_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_tenant_account_status CHECK (tenant_status IN ('ACTIVE','SUSPENDED','DELETED'))
);

-- Institution: Organization details for a tenant (CHILD)
CREATE TABLE institution (
    institution_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE DEFAULT app_current_tenant_id(),
    institution_name VARCHAR(255) NOT NULL,
    institution_code VARCHAR(50) UNIQUE,
    country VARCHAR(100),
    established_year INT,
    institution_type VARCHAR(50),
    website_url VARCHAR(500),
    logo_url VARCHAR(500),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_institution_name_length CHECK (LENGTH(institution_name) >= 3),
    CONSTRAINT ck_institution_year CHECK (established_year IS NULL OR established_year >= 1800)
);

-- TenantSettings: Tenant-specific configuration
CREATE TABLE tenant_settings (
    settings_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE DEFAULT app_current_tenant_id(),
    theme_color VARCHAR(7),
    banner_logo_url VARCHAR(500),
    support_email VARCHAR(255),
    from_email VARCHAR(255),
    max_upload_size_mb INT DEFAULT 100,
    session_timeout_minutes INT DEFAULT 30,
    password_min_length INT DEFAULT 8,
    require_mfa BOOLEAN DEFAULT FALSE,
    allow_self_registration BOOLEAN DEFAULT TRUE,
    maintenance_mode BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_tenant_settings_upload_size CHECK (max_upload_size_mb > 0),
    CONSTRAINT ck_tenant_settings_timeout CHECK (session_timeout_minutes > 0),
    CONSTRAINT ck_tenant_settings_password_length CHECK (password_min_length >= 6)
);

-- TenantBranding: Tenant branding and customization
CREATE TABLE tenant_branding (
    branding_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE DEFAULT app_current_tenant_id(),
    logo_url VARCHAR(500),
    favicon_url VARCHAR(500),
    primary_color VARCHAR(7),
    secondary_color VARCHAR(7),
    accent_color VARCHAR(7),
    font_family VARCHAR(100),
    custom_css TEXT,
    organization_tagline VARCHAR(500),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- TenantDomain: Custom domain names for tenants
CREATE TABLE tenant_domain (
    domain_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    domain_name VARCHAR(255) NOT NULL UNIQUE,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verification_token VARCHAR(255),
    date_verified TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_tenant_domain_format CHECK (domain_name ~ '^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?(\.[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?)*$')
);

-- TenantSubscription: Subscription and billing management
CREATE TABLE tenant_subscription (
    subscription_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE DEFAULT app_current_tenant_id(),
    plan_type VARCHAR(50) NOT NULL,
    plan_name VARCHAR(255),
    max_students INT,
    max_instructors INT,
    storage_limit_gb INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_start DATE NOT NULL,
    date_end DATE,
    date_renewal DATE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_tenant_subscription_plan CHECK (plan_type IN ('FREE', 'BASIC', 'PROFESSIONAL', 'ENTERPRISE')),
    CONSTRAINT ck_tenant_subscription_limits CHECK (max_students IS NULL OR max_students > 0),
    CONSTRAINT ck_tenant_subscription_dates CHECK (date_end IS NULL OR date_end >= date_start)
);

-- TenantUserLimit: User capacity limits per tenant
CREATE TABLE tenant_user_limit (
    limit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE DEFAULT app_current_tenant_id(),
    max_students INT,
    max_instructors INT,
    max_staff INT,
    max_admins INT,
    current_students INT DEFAULT 0,
    current_instructors INT DEFAULT 0,
    current_staff INT DEFAULT 0,
    current_admins INT DEFAULT 0,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_tenant_user_limit_max CHECK ( (max_students IS NULL OR max_students > 0) AND (max_instructors IS NULL OR max_instructors > 0) AND (max_staff IS NULL OR max_staff > 0) AND (max_admins IS NULL OR max_admins > 0) ),
    CONSTRAINT ck_tenant_user_limit_current CHECK ( current_students >= 0 AND current_instructors >= 0 AND current_staff >= 0 AND current_admins >= 0 )
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_tenant_is_active ON tenant(is_active);
CREATE INDEX idx_institution_name ON institution(institution_name);
CREATE INDEX idx_tenant_domain_tenant ON tenant_domain(tenant_id);
CREATE INDEX idx_tenant_domain_primary ON tenant_domain(is_primary) WHERE is_primary = TRUE;
CREATE INDEX idx_tenant_subscription_plan ON tenant_subscription(plan_type);
CREATE INDEX idx_tenant_subscription_is_active ON tenant_subscription(is_active);
CREATE INDEX idx_tenant_subscription_date_end ON tenant_subscription(date_end);
CREATE INDEX idx_tenant_user_limit_capacity ON tenant_user_limit(current_students, max_students);
CREATE UNIQUE INDEX uk_tenant_domain_one_primary ON tenant_domain(tenant_id) WHERE is_primary = TRUE;

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_tenant_date_updated BEFORE UPDATE ON tenant FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_institution_date_updated BEFORE UPDATE ON institution FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_tenant_settings_date_updated BEFORE UPDATE ON tenant_settings FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_tenant_branding_date_updated BEFORE UPDATE ON tenant_branding FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_tenant_subscription_date_updated BEFORE UPDATE ON tenant_subscription FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_tenant_user_limit_date_updated BEFORE UPDATE ON tenant_user_limit FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE tenant ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_tenant_tenant ON tenant FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE institution ENABLE ROW LEVEL SECURITY;
ALTER TABLE institution FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_institution_tenant ON institution FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE tenant_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_settings FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_tenant_settings_tenant ON tenant_settings FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE tenant_branding ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_branding FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_tenant_branding_tenant ON tenant_branding FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE tenant_domain ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_domain FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_tenant_domain_tenant ON tenant_domain FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE tenant_subscription ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_subscription FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_tenant_subscription_tenant ON tenant_subscription FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE tenant_user_limit ENABLE ROW LEVEL SECURITY;
ALTER TABLE tenant_user_limit FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_tenant_user_limit_tenant ON tenant_user_limit FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());

