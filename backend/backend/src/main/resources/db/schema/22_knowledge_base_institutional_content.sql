-- ================================================================================
-- 22. KNOWLEDGE BASE & INSTITUTIONAL CONTENT (10 tables)
-- ================================================================================
-- Tables: learning_object_repository, learning_object, learning_object_metadata, content_audit, course_library, open_educational_resource, knowledge_base, help_article, institutional_policy, policy_version
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- LearningObjectRepository: Repository for reusable learning objects
CREATE TABLE learning_object_repository (
    repository_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    repository_name VARCHAR(255) NOT NULL,
    repository_description TEXT,
    repository_type VARCHAR(50) NOT NULL,
    repository_url VARCHAR(500),
    is_public BOOLEAN DEFAULT FALSE,
    is_featured BOOLEAN DEFAULT FALSE,
    owner_id UUID,
    curator_id UUID,
    total_objects INT DEFAULT 0,
    featured_objects INT DEFAULT 0,
    total_downloads INT DEFAULT 0,
    average_rating NUMERIC(3,1),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    indexing_enabled BOOLEAN DEFAULT TRUE,
    last_indexed TIMESTAMPTZ,
    metadata_schema VARCHAR(100),
    repository_metadata JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_learning_object_repository_name CHECK (LENGTH(repository_name) >= 3),
    CONSTRAINT ck_learning_object_repository_type CHECK (repository_type IN ('INSTITUTIONAL', 'COURSE_LIBRARY', 'OER_COLLECTION', 'SHARED_COLLECTION', 'VENDOR', 'CUSTOM'))
);

-- LearningObject: Individual learning objects in repository
CREATE TABLE learning_object (
    object_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    repository_id UUID NOT NULL,
    object_name VARCHAR(255) NOT NULL,
    object_description TEXT,
    object_type VARCHAR(50) NOT NULL,
    subject_area VARCHAR(100),
    grade_level VARCHAR(50),
    learning_outcome TEXT,
    estimated_duration_minutes INT,
    difficulty_level VARCHAR(50),
    content_url VARCHAR(500),
    content_file_path VARCHAR(500),
    file_size_bytes INT,
    file_format VARCHAR(50),
    mime_type VARCHAR(100),
    language_code VARCHAR(10) DEFAULT 'en',
    is_accessible BOOLEAN DEFAULT TRUE,
    accessibility_features JSONB,
    creator_id UUID,
    creator_name VARCHAR(255),
    creator_organization VARCHAR(255),
    creation_date DATE,
    license_type VARCHAR(100),
    license_url VARCHAR(500),
    attribution_required BOOLEAN DEFAULT FALSE,
    usage_count INT DEFAULT 0,
    download_count INT DEFAULT 0,
    rating_average NUMERIC(3,1),
    rating_count INT DEFAULT 0,
    is_featured BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_approved BOOLEAN DEFAULT FALSE,
    approval_date TIMESTAMPTZ,
    approved_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_learning_object_name CHECK (LENGTH(object_name) >= 3),
    CONSTRAINT ck_learning_object_type CHECK (object_type IN ('VIDEO', 'INTERACTIVE', 'DOCUMENT', 'SIMULATION', 'ASSESSMENT', 'DISCUSSION_PROMPT', 'CASE_STUDY', 'SCENARIO', 'WORKSHEET', 'INFOGRAPHIC', 'PODCAST', 'ANIMATION', 'VIRTUAL_LAB')),
    CONSTRAINT ck_learning_object_difficulty CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT')),
    CONSTRAINT ck_learning_object_rating CHECK (rating_average IS NULL OR (rating_average >= 0 AND rating_average <= 5))
);

-- LearningObjectMetadata: Detailed metadata for learning objects
CREATE TABLE learning_object_metadata (
    metadata_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    object_id UUID NOT NULL UNIQUE,
    metadata_schema VARCHAR(100) NOT NULL,
    dublin_core_data JSONB,
    lom_data JSONB,
    scorm_metadata JSONB,
    custom_metadata JSONB,
    keywords JSONB,
    competencies JSONB,
    prerequisites TEXT,
    learning_objectives JSONB,
    interactivity_level VARCHAR(50),
    semantic_density VARCHAR(50),
    intended_end_user_role JSONB,
    context JSONB,
    typical_age_range VARCHAR(50),
    rights_and_restrictions TEXT,
    creation_context JSONB,
    educational_description TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_learning_object_metadata_schema CHECK (metadata_schema IN ('DUBLIN_CORE', 'LOM', 'SCORM', 'CUSTOM', 'MIAOU'))
);

-- ContentAudit: Audit trail for content changes and usage
CREATE TABLE content_audit (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    object_id UUID NOT NULL,
    audit_type VARCHAR(50) NOT NULL,
    auditor_id UUID,
    audit_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    audit_findings TEXT,
    accessibility_score NUMERIC(5,2),
    quality_score NUMERIC(5,2),
    relevance_score NUMERIC(5,2),
    accuracy_score NUMERIC(5,2),
    completeness_score NUMERIC(5,2),
    overall_score NUMERIC(5,2),
    issues_identified JSONB,
    recommendations TEXT,
    remediation_required BOOLEAN DEFAULT FALSE,
    remediation_deadline DATE,
    remediation_status VARCHAR(50),
    audit_status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_content_audit_type CHECK (audit_type IN ('QUALITY_REVIEW', 'ACCESSIBILITY_AUDIT', 'COMPLIANCE_CHECK', 'CURRENCY_REVIEW', 'USAGE_ANALYSIS')),
    CONSTRAINT ck_content_audit_status CHECK (audit_status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'FAILED'))
);

-- CourseLibrary: Curated library of complete courses
CREATE TABLE course_library (
    library_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    library_name VARCHAR(255) NOT NULL,
    library_description TEXT,
    library_type VARCHAR(50) NOT NULL,
    subject_area VARCHAR(100),
    curator_id UUID,
    total_courses INT DEFAULT 0,
    featured_courses INT DEFAULT 0,
    average_rating NUMERIC(3,1),
    total_enrollments INT DEFAULT 0,
    is_public BOOLEAN DEFAULT FALSE,
    is_featured BOOLEAN DEFAULT FALSE,
    requires_approval BOOLEAN DEFAULT FALSE,
    approval_process TEXT,
    distribution_method VARCHAR(50),
    update_frequency VARCHAR(50),
    last_updated TIMESTAMPTZ,
    library_guidelines TEXT,
    quality_standards TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_course_library_name CHECK (LENGTH(library_name) >= 3),
    CONSTRAINT ck_course_library_type CHECK (library_type IN ('INSTITUTIONAL', 'VENDOR', 'CONSORTIUM', 'COMMUNITY', 'CURATED_OER'))
);

-- OpenEducationalResource: Open Educational Resources (OER) management
CREATE TABLE open_educational_resource (
    oer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    oer_title VARCHAR(255) NOT NULL,
    oer_description TEXT,
    oer_type VARCHAR(50) NOT NULL,
    resource_url VARCHAR(500),
    creator_name VARCHAR(255),
    creator_organization VARCHAR(255),
    creator_contact VARCHAR(255),
    license_type VARCHAR(100) NOT NULL,
    license_url VARCHAR(500),
    is_derivative_ok BOOLEAN DEFAULT TRUE,
    is_commercial_use_ok BOOLEAN DEFAULT FALSE,
    attribution_text TEXT,
    version VARCHAR(50),
    language_code VARCHAR(10) DEFAULT 'en',
    subject_areas JSONB,
    grade_levels JSONB,
    learning_outcomes JSONB,
    accessibility_features JSONB,
    file_format VARCHAR(50),
    file_size_bytes INT,
    total_downloads INT DEFAULT 0,
    average_rating NUMERIC(3,1),
    rating_count INT DEFAULT 0,
    is_featured BOOLEAN DEFAULT FALSE,
    is_approved BOOLEAN DEFAULT FALSE,
    approval_date TIMESTAMPTZ,
    approved_by UUID,
    last_accessed TIMESTAMPTZ,
    usage_count INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_open_educational_resource_name CHECK (LENGTH(oer_title) >= 3),
    CONSTRAINT ck_open_educational_resource_type CHECK (oer_type IN ('TEXTBOOK', 'COURSE', 'MODULE', 'ACTIVITY', 'SIMULATION', 'DATASET', 'TOOLKIT', 'COLLECTION')),
    CONSTRAINT ck_open_educational_resource_license CHECK (license_type IN ('CC0', 'CC_BY', 'CC_BY_SA', 'CC_BY_NC', 'CC_BY_ND', 'CC_BY_NC_SA', 'CC_BY_NC_ND', 'PUBLIC_DOMAIN', 'OGL', 'MIT', 'APACHE2'))
);

-- KnowledgeBase: Institutional knowledge base for help and support
CREATE TABLE knowledge_base (
    kb_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    kb_name VARCHAR(255) NOT NULL,
    kb_description TEXT,
    kb_category VARCHAR(100),
    kb_icon_url VARCHAR(500),
    is_public BOOLEAN DEFAULT FALSE,
    requires_authentication BOOLEAN DEFAULT FALSE,
    featured BOOLEAN DEFAULT FALSE,
    view_count INT DEFAULT 0,
    search_enabled BOOLEAN DEFAULT TRUE,
    ai_powered_search BOOLEAN DEFAULT FALSE,
    total_articles INT DEFAULT 0,
    last_updated TIMESTAMPTZ,
    owner_id UUID,
    maintainer_id UUID,
    seo_title VARCHAR(255),
    seo_description VARCHAR(500),
    seo_keywords VARCHAR(255),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_knowledge_base_name CHECK (LENGTH(kb_name) >= 3)
);

-- HelpArticle: Articles and documentation in knowledge base
CREATE TABLE help_article (
    article_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    kb_id UUID NOT NULL,
    article_title VARCHAR(255) NOT NULL,
    article_slug VARCHAR(255) UNIQUE,
    article_content TEXT NOT NULL,
    article_html JSONB,
    article_category VARCHAR(100),
    article_tags JSONB,
    author_id UUID NOT NULL,
    editor_id UUID,
    last_edited_at TIMESTAMPTZ,
    article_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    published_date TIMESTAMPTZ,
    is_featured BOOLEAN DEFAULT FALSE,
    is_pinned BOOLEAN DEFAULT FALSE,
    helpful_count INT DEFAULT 0,
    unhelpful_count INT DEFAULT 0,
    view_count INT DEFAULT 0,
    search_keywords VARCHAR(500),
    related_article_ids JSONB,
    attachments_count INT DEFAULT 0,
    language_code VARCHAR(10) DEFAULT 'en',
    version_number INT DEFAULT 1,
    seo_title VARCHAR(255),
    seo_description VARCHAR(500),
    estimated_read_time_minutes INT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_help_article_title CHECK (LENGTH(article_title) >= 3),
    CONSTRAINT ck_help_article_status CHECK (article_status IN ('DRAFT', 'UNDER_REVIEW', 'PUBLISHED', 'ARCHIVED', 'DEPRECATED'))
);

-- InstitutionalPolicy: Institution-wide policies and procedures
CREATE TABLE institutional_policy (
    policy_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    policy_title VARCHAR(255) NOT NULL,
    policy_code VARCHAR(100),
    policy_category VARCHAR(100) NOT NULL,
    policy_description TEXT,
    policy_content TEXT NOT NULL,
    policy_scope VARCHAR(100),
    effective_date DATE NOT NULL,
    expiration_date DATE,
    approval_date TIMESTAMPTZ,
    approved_by UUID,
    policy_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    requires_acknowledgment BOOLEAN DEFAULT FALSE,
    acknowledgment_deadline DATE,
    owner_department VARCHAR(255),
    responsible_user_id UUID,
    revision_history JSONB,
    related_policies JSONB,
    policy_url VARCHAR(500),
    compliance_requirement VARCHAR(100),
    enforcement_method TEXT,
    penalty_for_violation TEXT,
    seo_keywords VARCHAR(255),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_institutional_policy_title CHECK (LENGTH(policy_title) >= 3),
    CONSTRAINT ck_institutional_policy_category CHECK (policy_category IN ('ACADEMIC', 'CONDUCT', 'TECHNOLOGY', 'FINANCE', 'HUMAN_RESOURCES', 'SAFETY', 'ACCESSIBILITY', 'PRIVACY', 'INTELLECTUAL_PROPERTY', 'GENERAL')),
    CONSTRAINT ck_institutional_policy_status CHECK (policy_status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'PUBLISHED', 'UNDER_REVISION', 'ARCHIVED')),
    CONSTRAINT ck_institutional_policy_dates CHECK (expiration_date IS NULL OR expiration_date >= effective_date)
);

-- PolicyVersion: Version history and audit trail for policies
CREATE TABLE policy_version (
    version_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    policy_id UUID NOT NULL,
    version_number INT NOT NULL,
    version_title VARCHAR(255),
    version_content TEXT NOT NULL,
    previous_version_id UUID,
    change_summary TEXT,
    change_type VARCHAR(50),
    created_by UUID NOT NULL,
    created_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_current BOOLEAN DEFAULT FALSE,
    approval_date TIMESTAMPTZ,
    approved_by UUID,
    effective_date DATE,
    expiration_date DATE,
    version_metadata JSONB,
    CONSTRAINT uk_policy_version UNIQUE (policy_id, version_number),
    CONSTRAINT ck_policy_version_change_type CHECK (change_type IN ('CREATION', 'MINOR_UPDATE', 'MAJOR_REVISION', 'CORRECTION', 'ARCHIVAL'))
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_learning_object_repository_tenant ON learning_object_repository(tenant_id);
CREATE INDEX idx_learning_object_repository_type ON learning_object_repository(repository_type);
CREATE INDEX idx_learning_object_repository_is_featured ON learning_object_repository(is_featured) WHERE is_featured = TRUE;
CREATE INDEX idx_learning_object_repository_owner ON learning_object_repository(owner_id);
CREATE INDEX idx_learning_object_repository ON learning_object(repository_id);
CREATE INDEX idx_learning_object_type ON learning_object(object_type);
CREATE INDEX idx_learning_object_creator ON learning_object(creator_id);
CREATE INDEX idx_learning_object_is_featured ON learning_object(is_featured) WHERE is_featured = TRUE;
CREATE INDEX idx_learning_object_rating ON learning_object(rating_average DESC);
CREATE INDEX idx_learning_object_metadata_schema ON learning_object_metadata(metadata_schema);
CREATE INDEX idx_content_audit_object ON content_audit(object_id);
CREATE INDEX idx_content_audit_type ON content_audit(audit_type);
CREATE INDEX idx_content_audit_auditor ON content_audit(auditor_id);
CREATE INDEX idx_content_audit_date ON content_audit(audit_date DESC);
CREATE INDEX idx_course_library_tenant ON course_library(tenant_id);
CREATE INDEX idx_course_library_type ON course_library(library_type);
CREATE INDEX idx_course_library_is_featured ON course_library(is_featured) WHERE is_featured = TRUE;
CREATE INDEX idx_open_educational_resource_tenant ON open_educational_resource(tenant_id);
CREATE INDEX idx_open_educational_resource_type ON open_educational_resource(oer_type);
CREATE INDEX idx_open_educational_resource_license ON open_educational_resource(license_type);
CREATE INDEX idx_open_educational_resource_is_featured ON open_educational_resource(is_featured) WHERE is_featured = TRUE;
CREATE INDEX idx_knowledge_base_tenant ON knowledge_base(tenant_id);
CREATE INDEX idx_knowledge_base_category ON knowledge_base(kb_category);
CREATE INDEX idx_knowledge_base_is_public ON knowledge_base(is_public) WHERE is_public = TRUE;
CREATE INDEX idx_knowledge_base_featured ON knowledge_base(featured) WHERE featured = TRUE;
CREATE INDEX idx_help_article_kb ON help_article(kb_id);
CREATE INDEX idx_help_article_author ON help_article(author_id);
CREATE INDEX idx_help_article_status ON help_article(article_status);
CREATE INDEX idx_help_article_is_published ON help_article(is_published) WHERE is_published = TRUE;
CREATE INDEX idx_help_article_category ON help_article(article_category);
CREATE INDEX idx_help_article_view_count ON help_article(view_count DESC);
CREATE INDEX idx_institutional_policy_tenant ON institutional_policy(tenant_id);
CREATE INDEX idx_institutional_policy_category ON institutional_policy(policy_category);
CREATE INDEX idx_institutional_policy_status ON institutional_policy(policy_status);
CREATE INDEX idx_institutional_policy_is_published ON institutional_policy(is_published) WHERE is_published = TRUE;
CREATE INDEX idx_institutional_policy_code ON institutional_policy(policy_code);
CREATE INDEX idx_policy_version_policy ON policy_version(policy_id);
CREATE INDEX idx_policy_version_created_by ON policy_version(created_by);
CREATE INDEX idx_policy_version_is_current ON policy_version(is_current) WHERE is_current = TRUE;
CREATE INDEX idx_policy_version_approved_by ON policy_version(approved_by);
CREATE INDEX idx_learning_object_tenant ON learning_object(tenant_id);
CREATE INDEX idx_learning_object_metadata_tenant ON learning_object_metadata(tenant_id);
CREATE INDEX idx_content_audit_tenant ON content_audit(tenant_id);
CREATE INDEX idx_help_article_tenant ON help_article(tenant_id);
CREATE INDEX idx_policy_version_tenant ON policy_version(tenant_id);
CREATE INDEX idx_course_library_curator_id_fk ON course_library(curator_id);
CREATE INDEX idx_help_article_editor_id_fk ON help_article(editor_id);
CREATE INDEX idx_institutional_policy_approved_by_fk ON institutional_policy(approved_by);
CREATE INDEX idx_institutional_policy_responsible_user_id_fk ON institutional_policy(responsible_user_id);
CREATE INDEX idx_knowledge_base_maintainer_id_fk ON knowledge_base(maintainer_id);
CREATE INDEX idx_knowledge_base_owner_id_fk ON knowledge_base(owner_id);
CREATE INDEX idx_learning_object_approved_by_fk ON learning_object(approved_by);
CREATE INDEX idx_learning_object_repository_curator_id_fk ON learning_object_repository(curator_id);
CREATE INDEX idx_open_educational_resource_approved_by_fk ON open_educational_resource(approved_by);
CREATE INDEX idx_policy_version_previous_version_id_fk ON policy_version(previous_version_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_learning_object_repository_date_updated BEFORE UPDATE ON learning_object_repository FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_learning_object_date_updated BEFORE UPDATE ON learning_object FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_learning_object_metadata_date_updated BEFORE UPDATE ON learning_object_metadata FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_course_library_date_updated BEFORE UPDATE ON course_library FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_open_educational_resource_date_updated BEFORE UPDATE ON open_educational_resource FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_knowledge_base_date_updated BEFORE UPDATE ON knowledge_base FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_help_article_date_updated BEFORE UPDATE ON help_article FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_institutional_policy_date_updated BEFORE UPDATE ON institutional_policy FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE learning_object_repository ENABLE ROW LEVEL SECURITY;
ALTER TABLE learning_object_repository FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_learning_object_repository_tenant ON learning_object_repository FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE learning_object ENABLE ROW LEVEL SECURITY;
ALTER TABLE learning_object FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_learning_object_tenant ON learning_object FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE learning_object_metadata ENABLE ROW LEVEL SECURITY;
ALTER TABLE learning_object_metadata FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_learning_object_metadata_tenant ON learning_object_metadata FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE content_audit ENABLE ROW LEVEL SECURITY;
ALTER TABLE content_audit FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_content_audit_tenant ON content_audit FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE course_library ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_library FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_course_library_tenant ON course_library FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE open_educational_resource ENABLE ROW LEVEL SECURITY;
ALTER TABLE open_educational_resource FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_open_educational_resource_tenant ON open_educational_resource FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE knowledge_base ENABLE ROW LEVEL SECURITY;
ALTER TABLE knowledge_base FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_knowledge_base_tenant ON knowledge_base FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE help_article ENABLE ROW LEVEL SECURITY;
ALTER TABLE help_article FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_help_article_tenant ON help_article FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE institutional_policy ENABLE ROW LEVEL SECURITY;
ALTER TABLE institutional_policy FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_institutional_policy_tenant ON institutional_policy FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE policy_version ENABLE ROW LEVEL SECURITY;
ALTER TABLE policy_version FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_policy_version_tenant ON policy_version FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
