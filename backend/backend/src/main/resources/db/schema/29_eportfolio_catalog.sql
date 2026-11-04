-- ================================================================================
-- 29. EPORTFOLIO & CATALOG (6 tables)
-- ================================================================================
-- Tables: portfolio, portfolio_section, portfolio_artifact, portfolio_share, catalog_listing, catalog_order
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
CREATE TABLE portfolio (
    portfolio_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    portfolio_title VARCHAR(255) NOT NULL,
    portfolio_description TEXT,
    template_portfolio_id UUID,
    course_id UUID,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    public_slug VARCHAR(255),
    is_template BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_portfolio_slug UNIQUE (tenant_id, public_slug),
    CONSTRAINT ck_portfolio_visibility CHECK (visibility IN ('PRIVATE','INSTITUTION','LINK','PUBLIC'))
);

CREATE TABLE portfolio_section (
    portfolio_section_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    portfolio_id UUID NOT NULL,
    section_title VARCHAR(255) NOT NULL,
    body_html TEXT,
    display_order INT NOT NULL DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE portfolio_artifact (
    portfolio_artifact_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    portfolio_section_id UUID NOT NULL,
    artifact_title VARCHAR(255) NOT NULL,
    reflection TEXT,
    submission_id UUID,
    file_id UUID,
    student_badge_id UUID,
    external_url VARCHAR(1000),
    outcome_id UUID,
    display_order INT NOT NULL DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE portfolio_share (
    portfolio_share_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    portfolio_id UUID NOT NULL,
    shared_with_user_id UUID,
    shared_with_email CITEXT,
    access_token_hash VARCHAR(255),
    can_comment BOOLEAN NOT NULL DEFAULT FALSE,
    date_expires TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_portfolio_share_target CHECK (num_nonnulls(shared_with_user_id, shared_with_email, access_token_hash) >= 1)
);

CREATE TABLE catalog_listing (
    listing_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    section_id UUID,
    listing_title VARCHAR(255) NOT NULL,
    listing_slug VARCHAR(255) NOT NULL,
    summary TEXT,
    listing_image_url VARCHAR(1000),
    price_amount NUMERIC(10,2) NOT NULL DEFAULT 0,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    capacity INT,
    is_visible BOOLEAN NOT NULL DEFAULT FALSE,
    enrollment_open BOOLEAN NOT NULL DEFAULT FALSE,
    date_enroll_start TIMESTAMPTZ,
    date_enroll_end TIMESTAMPTZ,
    awards_badge_id UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_catalog_listing_slug UNIQUE (tenant_id, listing_slug),
    CONSTRAINT ck_catalog_listing_price CHECK (price_amount >= 0),
    CONSTRAINT ck_catalog_listing_capacity CHECK (capacity IS NULL OR capacity > 0)
);

CREATE TABLE catalog_order (
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    listing_id UUID NOT NULL,
    user_id UUID NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    payment_provider VARCHAR(30),
    payment_reference VARCHAR(255),
    order_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    enrollment_id UUID,
    date_paid TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_catalog_order_amount CHECK (amount >= 0),
    CONSTRAINT ck_catalog_order_status CHECK (order_status IN ('PENDING','PAID','FREE','REFUNDED','FAILED','CANCELLED'))
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_catalog_listing_awards_badge_id_fk ON catalog_listing(awards_badge_id);
CREATE INDEX idx_catalog_listing_course_id_fk ON catalog_listing(course_id);
CREATE INDEX idx_catalog_listing_section_id_fk ON catalog_listing(section_id);
CREATE INDEX idx_catalog_order_enrollment_id_fk ON catalog_order(enrollment_id);
CREATE INDEX idx_catalog_order_listing_id_fk ON catalog_order(listing_id);
CREATE INDEX idx_catalog_order_tenant_id_fk ON catalog_order(tenant_id);
CREATE INDEX idx_catalog_order_user_id_fk ON catalog_order(user_id);
CREATE INDEX idx_portfolio_artifact_file_id_fk ON portfolio_artifact(file_id);
CREATE INDEX idx_portfolio_artifact_outcome_id_fk ON portfolio_artifact(outcome_id);
CREATE INDEX idx_portfolio_artifact_portfolio_section_id_fk ON portfolio_artifact(portfolio_section_id);
CREATE INDEX idx_portfolio_artifact_student_badge_id_fk ON portfolio_artifact(student_badge_id);
CREATE INDEX idx_portfolio_artifact_submission_id_fk ON portfolio_artifact(submission_id);
CREATE INDEX idx_portfolio_artifact_tenant_id_fk ON portfolio_artifact(tenant_id);
CREATE INDEX idx_portfolio_course_id_fk ON portfolio(course_id);
CREATE INDEX idx_portfolio_section_portfolio_id_fk ON portfolio_section(portfolio_id);
CREATE INDEX idx_portfolio_section_tenant_id_fk ON portfolio_section(tenant_id);
CREATE INDEX idx_portfolio_share_portfolio_id_fk ON portfolio_share(portfolio_id);
CREATE INDEX idx_portfolio_share_shared_with_user_id_fk ON portfolio_share(shared_with_user_id);
CREATE INDEX idx_portfolio_share_tenant_id_fk ON portfolio_share(tenant_id);
CREATE INDEX idx_portfolio_template_portfolio_id_fk ON portfolio(template_portfolio_id);
CREATE INDEX idx_portfolio_user_id_fk ON portfolio(user_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_portfolio_date_updated BEFORE UPDATE ON portfolio FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_portfolio_section_date_updated BEFORE UPDATE ON portfolio_section FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_portfolio_artifact_date_updated BEFORE UPDATE ON portfolio_artifact FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_catalog_listing_date_updated BEFORE UPDATE ON catalog_listing FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_catalog_order_date_updated BEFORE UPDATE ON catalog_order FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE portfolio ENABLE ROW LEVEL SECURITY;
ALTER TABLE portfolio FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_portfolio_tenant ON portfolio FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE portfolio_section ENABLE ROW LEVEL SECURITY;
ALTER TABLE portfolio_section FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_portfolio_section_tenant ON portfolio_section FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE portfolio_artifact ENABLE ROW LEVEL SECURITY;
ALTER TABLE portfolio_artifact FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_portfolio_artifact_tenant ON portfolio_artifact FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE portfolio_share ENABLE ROW LEVEL SECURITY;
ALTER TABLE portfolio_share FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_portfolio_share_tenant ON portfolio_share FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE catalog_listing ENABLE ROW LEVEL SECURITY;
ALTER TABLE catalog_listing FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_catalog_listing_tenant ON catalog_listing FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE catalog_order ENABLE ROW LEVEL SECURITY;
ALTER TABLE catalog_order FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_catalog_order_tenant ON catalog_order FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
