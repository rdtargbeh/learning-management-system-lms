-- ================================================================================
-- 24. COMMENTS & REACTIONS (3 tables)
-- ================================================================================
-- Tables: comment, comment_reaction, comment_mention
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- Comment: Generic comments on any entity
CREATE TABLE comment (
    comment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    user_id UUID NOT NULL,
    parent_comment_id UUID,
    comment_text TEXT NOT NULL,
    is_private BOOLEAN DEFAULT FALSE,
    is_edited BOOLEAN DEFAULT FALSE,
    edited_at TIMESTAMPTZ,
    edited_by UUID,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_comment_text CHECK (LENGTH(comment_text) >= 1)
);

-- CommentReaction: Likes, emojis on comments
CREATE TABLE comment_reaction (
    reaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    comment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reaction_type VARCHAR(50),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_comment_reaction UNIQUE (comment_id, user_id)
);

-- CommentMention: @mentions in comments
CREATE TABLE comment_mention (
    mention_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    comment_id UUID NOT NULL,
    mentioned_user_id UUID NOT NULL,
    mention_position INT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_comment_entity ON comment(entity_type, entity_id);
CREATE INDEX idx_comment_user ON comment(user_id);
CREATE INDEX idx_comment_parent ON comment(parent_comment_id);
CREATE INDEX idx_comment_date ON comment(date_created DESC);
CREATE INDEX idx_comment_reaction_tenant ON comment_reaction(tenant_id);
CREATE INDEX idx_comment_reaction_comment ON comment_reaction(comment_id);
CREATE INDEX idx_comment_reaction_user ON comment_reaction(user_id);
CREATE INDEX idx_comment_mention_tenant ON comment_mention(tenant_id);
CREATE INDEX idx_comment_mention_comment ON comment_mention(comment_id);
CREATE INDEX idx_comment_mention_user ON comment_mention(mentioned_user_id);
CREATE INDEX idx_comment_edited_by_fk ON comment(edited_by);
CREATE INDEX idx_comment_tenant_id_fk ON comment(tenant_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_comment_date_updated BEFORE UPDATE ON comment FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE comment ENABLE ROW LEVEL SECURITY;
ALTER TABLE comment FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_comment_tenant ON comment FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE comment_reaction ENABLE ROW LEVEL SECURITY;
ALTER TABLE comment_reaction FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_comment_reaction_tenant ON comment_reaction FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE comment_mention ENABLE ROW LEVEL SECURITY;
ALTER TABLE comment_mention FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_comment_mention_tenant ON comment_mention FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
