-- ================================================================================
-- 30. AI FEATURES (1 tables)
-- ================================================================================
-- Tables: ai_interaction
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
CREATE TABLE ai_interaction (
    ai_interaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID,
    course_id UUID,
    feature_key VARCHAR(50) NOT NULL,
    model_provider VARCHAR(50),
    model_name VARCHAR(100),
    entity_type VARCHAR(30),
    entity_id UUID,
    input_tokens INT,
    output_tokens INT,
    latency_ms INT,
    interaction_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    output_accepted BOOLEAN,
    reviewed_by UUID,
    safety_flags JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_ai_interaction_feature CHECK (feature_key IN ('COURSE_DESIGN','QUIZ_GENERATION','RUBRIC_GENERATION','DISCUSSION_SUMMARY','DISCUSSION_INSIGHTS', 'TRANSLATION','ALT_TEXT','ACCESSIBILITY_FIX','GRADING_ASSIST','TUTOR_CHAT','CONTENT_SEARCH','OTHER')),
    CONSTRAINT ck_ai_interaction_status CHECK (interaction_status IN ('COMPLETED','FAILED','BLOCKED','PENDING_REVIEW'))
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_ai_interaction_feature_date ON ai_interaction(tenant_id, feature_key, date_created);
CREATE INDEX idx_ai_interaction_course_id_fk ON ai_interaction(course_id);
CREATE INDEX idx_ai_interaction_reviewed_by_fk ON ai_interaction(reviewed_by);
CREATE INDEX idx_ai_interaction_user_id_fk ON ai_interaction(user_id);

-- ---------------------------------------------------------------- TRIGGERS


-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE ai_interaction ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_interaction FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_ai_interaction_tenant ON ai_interaction FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
