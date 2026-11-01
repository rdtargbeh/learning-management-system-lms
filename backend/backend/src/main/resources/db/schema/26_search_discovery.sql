-- ================================================================================
-- 26. SEARCH & DISCOVERY (2 tables)
-- ================================================================================
-- Tables: search_index, saved_search
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- SearchIndex: Full-text search index
CREATE TABLE search_index (
    search_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    searchable_title VARCHAR(500),
    searchable_content TEXT,
    entity_url VARCHAR(500),
    date_indexed TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- SavedSearch: User saved searches
CREATE TABLE saved_search (
    saved_search_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    search_name VARCHAR(255),
    search_query TEXT,
    search_filters JSONB,
    is_favorite BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_search_index_entity ON search_index(entity_type, entity_id);
CREATE INDEX idx_search_index_tenant ON search_index(tenant_id);
CREATE INDEX idx_saved_search_tenant ON saved_search(tenant_id);
CREATE INDEX idx_saved_search_user ON saved_search(user_id);
CREATE INDEX idx_saved_search_favorite ON saved_search(is_favorite);

-- ---------------------------------------------------------------- TRIGGERS


-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE search_index ENABLE ROW LEVEL SECURITY;
ALTER TABLE search_index FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_search_index_tenant ON search_index FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE saved_search ENABLE ROW LEVEL SECURITY;
ALTER TABLE saved_search FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_saved_search_tenant ON saved_search FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
