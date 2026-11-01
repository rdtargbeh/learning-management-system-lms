-- ================================================================================
-- 20. LEARNING STANDARDS & COMPLIANCE (10 tables)
-- ================================================================================
-- Tables: educational_standard, standards_framework, content_standard_alignment, outcome_standard_alignment, compliance_requirement, accessibility_standard, data_privacy_policy, accreditation_metric, outcome_result, outcome_proficiency_rating
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- EducationalStandard: Educational standards and competencies
CREATE TABLE educational_standard (
    standard_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    standard_name VARCHAR(255) NOT NULL,
    standard_code VARCHAR(100) UNIQUE,
    standard_description TEXT,
    standard_type VARCHAR(50) NOT NULL,
    subject_area VARCHAR(100),
    grade_level VARCHAR(50),
    academic_level VARCHAR(50),
    version VARCHAR(50),
    source_organization VARCHAR(255),
    source_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    alignment_difficulty NUMERIC(3,1),
    assessment_method VARCHAR(100),
    parent_standard_id UUID,
    learning_outcomes JSONB,
    competency_level VARCHAR(50),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_educational_standard_name CHECK (LENGTH(standard_name) >= 3),
    CONSTRAINT ck_educational_standard_type CHECK (standard_type IN ('BLOOM_TAXONOMY', 'CCSS', 'NGSS', 'ISTE', 'ACRL', 'CUSTOM', 'COMPETENCY_FRAMEWORK', 'LEARNING_OUTCOME')),
    CONSTRAINT ck_educational_standard_competency CHECK (competency_level IN ('FOUNDATIONAL', 'INTERMEDIATE', 'ADVANCED', 'MASTERY'))
);

-- StandardsFramework: Framework organizing multiple standards
CREATE TABLE standards_framework (
    framework_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    framework_name VARCHAR(255) NOT NULL,
    framework_description TEXT,
    framework_type VARCHAR(50) NOT NULL,
    framework_version VARCHAR(50),
    source_organization VARCHAR(255),
    source_url VARCHAR(500),
    adoption_status VARCHAR(50) DEFAULT 'DRAFT',
    adoption_date DATE,
    review_date DATE,
    next_review_date DATE,
    is_primary_framework BOOLEAN DEFAULT FALSE,
    total_standards INT,
    mapped_courses INT DEFAULT 0,
    mapped_content_count INT DEFAULT 0,
    framework_structure JSONB,
    implementation_guidelines TEXT,
    is_required_by_accreditor BOOLEAN DEFAULT FALSE,
    accreditor_name VARCHAR(255),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_standards_framework_name CHECK (LENGTH(framework_name) >= 3),
    CONSTRAINT ck_standards_framework_type CHECK (framework_type IN ('CONTENT_STANDARDS', 'LEARNING_OUTCOMES', 'COMPETENCY_FRAMEWORK', 'ACCREDITATION_STANDARDS', 'NATIONAL_STANDARDS')),
    CONSTRAINT ck_standards_framework_adoption CHECK (adoption_status IN ('DRAFT', 'PENDING_APPROVAL', 'ADOPTED', 'DEPRECATED', 'ARCHIVED'))
);

-- ContentStandardAlignment: Alignment of course content to standards
CREATE TABLE content_standard_alignment (
    alignment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    content_id UUID NOT NULL,
    standard_id UUID NOT NULL,
    framework_id UUID,
    alignment_strength VARCHAR(50) NOT NULL,
    alignment_percentage NUMERIC(5,2),
    alignment_notes TEXT,
    assessment_method VARCHAR(100),
    assessment_tool_id UUID,
    is_primary_alignment BOOLEAN DEFAULT FALSE,
    alignment_verified BOOLEAN DEFAULT FALSE,
    verified_by UUID,
    verification_date TIMESTAMPTZ,
    review_date DATE,
    alignment_status VARCHAR(50) DEFAULT 'ALIGNED',
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_content_standard_alignment UNIQUE (content_id, standard_id),
    CONSTRAINT ck_content_standard_alignment_strength CHECK (alignment_strength IN ('FULL', 'PARTIAL', 'MINIMAL', 'INDIRECT')),
    CONSTRAINT ck_content_standard_alignment_percentage CHECK (alignment_percentage IS NULL OR (alignment_percentage >= 0 AND alignment_percentage <= 100)),
    CONSTRAINT ck_content_standard_alignment_status CHECK (alignment_status IN ('ALIGNED', 'PARTIAL', 'NEEDS_REVIEW', 'NOT_ALIGNED'))
);

-- OutcomeStandardAlignment: Alignment of learning outcomes to standards
CREATE TABLE outcome_standard_alignment (
    alignment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_outcome_id UUID,
    assessment_id UUID,
    standard_id UUID NOT NULL,
    framework_id UUID,
    alignment_strength VARCHAR(50) NOT NULL,
    achievement_threshold NUMERIC(5,2),
    mastery_indicator TEXT,
    assessment_evidence TEXT,
    alignment_notes TEXT,
    is_primary_outcome BOOLEAN DEFAULT FALSE,
    alignment_verified BOOLEAN DEFAULT FALSE,
    verified_by UUID,
    verification_date TIMESTAMPTZ,
    expected_proficiency_level VARCHAR(50),
    actual_proficiency_level VARCHAR(50),
    proficiency_data JSONB,
    alignment_status VARCHAR(50) DEFAULT 'ALIGNED',
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_outcome_standard_alignment_strength CHECK (alignment_strength IN ('FULL', 'PARTIAL', 'MINIMAL', 'INDIRECT')),
    CONSTRAINT ck_outcome_standard_alignment_threshold CHECK (achievement_threshold IS NULL OR (achievement_threshold >= 0 AND achievement_threshold <= 100))
);

-- ComplianceRequirement: Compliance requirements and regulations
CREATE TABLE compliance_requirement (
    requirement_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    requirement_name VARCHAR(255) NOT NULL,
    requirement_description TEXT,
    requirement_type VARCHAR(50) NOT NULL,
    jurisdiction VARCHAR(100),
    applicable_regulations JSONB,
    is_mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    effective_date DATE,
    expiration_date DATE,
    compliance_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    responsible_department VARCHAR(255),
    responsible_user_id UUID,
    evidence_required TEXT,
    evidence_submission_deadline DATE,
    audit_required BOOLEAN DEFAULT TRUE,
    audit_frequency VARCHAR(50),
    penalty_for_non_compliance TEXT,
    resource_links JSONB,
    implementation_notes TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_compliance_requirement_name CHECK (LENGTH(requirement_name) >= 3),
    CONSTRAINT ck_compliance_requirement_type CHECK (requirement_type IN ('ACCESSIBILITY', 'DATA_PRIVACY', 'FERPA', 'HIPAA', 'GDPR', 'CCPA', 'ADA', 'ACCREDITATION', 'STATE_REGULATION', 'INSTITUTIONAL')),
    CONSTRAINT ck_compliance_requirement_status CHECK (compliance_status IN ('PENDING', 'IN_PROGRESS', 'COMPLIANT', 'NON_COMPLIANT', 'WAIVER_GRANTED')),
    CONSTRAINT ck_compliance_requirement_dates CHECK (expiration_date IS NULL OR expiration_date >= effective_date)
);

-- AccessibilityStandard: Accessibility standards and WCAG compliance
CREATE TABLE accessibility_standard (
    standard_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    standard_name VARCHAR(255) NOT NULL,
    standard_code VARCHAR(50),
    wcag_level VARCHAR(10),
    wcag_criteria VARCHAR(50),
    standard_description TEXT,
    standard_type VARCHAR(50) NOT NULL,
    applicable_to JSONB,
    compliance_checklist JSONB,
    testing_procedure TEXT,
    remediation_guidance TEXT,
    tools_required JSONB,
    is_critical BOOLEAN DEFAULT FALSE,
    is_auditable BOOLEAN DEFAULT TRUE,
    current_compliance_percentage NUMERIC(5,2),
    last_audit_date DATE,
    next_audit_date DATE,
    audit_findings JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_accessibility_standard_name CHECK (LENGTH(standard_name) >= 3),
    CONSTRAINT ck_accessibility_standard_type CHECK (standard_type IN ('COLOR_CONTRAST', 'KEYBOARD_NAVIGATION', 'SCREEN_READER', 'CAPTIONS', 'TRANSCRIPTS', 'ALT_TEXT', 'FORM_LABELS', 'FOCUS_INDICATOR', 'READABILITY', 'MOBILE_ACCESSIBILITY')),
    CONSTRAINT ck_accessibility_standard_wcag CHECK (wcag_level IS NULL OR wcag_level IN ('A', 'AA', 'AAA')),
    CONSTRAINT ck_accessibility_standard_compliance CHECK (current_compliance_percentage IS NULL OR (current_compliance_percentage >= 0 AND current_compliance_percentage <= 100))
);

-- DataPrivacyPolicy: Data privacy and FERPA compliance
CREATE TABLE data_privacy_policy (
    policy_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL UNIQUE DEFAULT app_current_tenant_id(),
    policy_name VARCHAR(255) NOT NULL,
    policy_version VARCHAR(50),
    policy_description TEXT,
    policy_type VARCHAR(50) NOT NULL,
    jurisdiction VARCHAR(100),
    applicable_laws JSONB,
    data_collection_practices TEXT,
    data_retention_policy TEXT,
    data_retention_period_days INT,
    third_party_sharing_policy TEXT,
    student_rights_policy TEXT,
    parental_access_policy TEXT,
    data_breach_procedure TEXT,
    encryption_standard VARCHAR(100),
    access_logging_enabled BOOLEAN DEFAULT TRUE,
    audit_logging_enabled BOOLEAN DEFAULT TRUE,
    data_security_measures JSONB,
    is_published BOOLEAN DEFAULT FALSE,
    approved_date TIMESTAMPTZ,
    approved_by UUID,
    review_frequency VARCHAR(50),
    next_review_date DATE,
    policy_url VARCHAR(500),
    privacy_officer_id UUID,
    contact_email VARCHAR(255),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_data_privacy_policy_name CHECK (LENGTH(policy_name) >= 3),
    CONSTRAINT ck_data_privacy_policy_type CHECK (policy_type IN ('FERPA', 'COPPA', 'GDPR', 'CCPA', 'HIPAA', 'INSTITUTIONAL')),
    CONSTRAINT ck_data_privacy_policy_retention CHECK (data_retention_period_days IS NULL OR data_retention_period_days > 0)
);

-- AccreditationMetric: Metrics and indicators for accreditation
CREATE TABLE accreditation_metric (
    metric_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    metric_name VARCHAR(255) NOT NULL,
    metric_description TEXT,
    metric_type VARCHAR(50) NOT NULL,
    accreditation_body VARCHAR(255),
    metric_code VARCHAR(100),
    target_value NUMERIC(10,2),
    current_value NUMERIC(10,2),
    measurement_unit VARCHAR(50),
    data_source VARCHAR(100),
    collection_method VARCHAR(100),
    collection_frequency VARCHAR(50),
    last_measured DATE,
    next_measurement DATE,
    measurement_data JSONB,
    evidence_location VARCHAR(500),
    is_met BOOLEAN,
    variance_percent NUMERIC(8,2),
    improvement_plan TEXT,
    responsible_department VARCHAR(255),
    responsible_user_id UUID,
    benchmark_value NUMERIC(10,2),
    peer_value NUMERIC(10,2),
    trending JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_accreditation_metric_name CHECK (LENGTH(metric_name) >= 3),
    CONSTRAINT ck_accreditation_metric_type CHECK (metric_type IN ('STUDENT_LEARNING', 'RETENTION', 'COMPLETION', 'EMPLOYMENT', 'SATISFACTION', 'PROGRAM_QUALITY', 'FACULTY_QUALITY', 'RESOURCE_ADEQUACY', 'INFRASTRUCTURE'))
);

-- OUTCOME RESULT: per-student mastery of a learning outcome (Canvas Learning Mastery Gradebook)
CREATE TABLE outcome_result (
    outcome_result_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    outcome_id UUID NOT NULL,
    user_id UUID NOT NULL,
    course_id UUID NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    source_id UUID,
    score NUMERIC(8,2),
    possible NUMERIC(8,2),
    mastery_level VARCHAR(30),
    is_mastered BOOLEAN,
    date_assessed TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_outcome_result_source CHECK (source_type IN ('RUBRIC','ASSESSMENT','QUESTION','ASSIGNMENT','MANUAL','LTI')),
    CONSTRAINT ck_outcome_result_score CHECK (score IS NULL OR possible IS NULL OR score <= possible)
);

CREATE TABLE outcome_proficiency_rating (
    rating_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    org_unit_id UUID,
    course_id UUID,
    rating_description VARCHAR(100) NOT NULL,
    points NUMERIC(8,2) NOT NULL,
    color VARCHAR(20),
    is_mastery BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_outcome_proficiency_rating_owner CHECK (num_nonnulls(org_unit_id, course_id) <= 1)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_educational_standard_tenant ON educational_standard(tenant_id);
CREATE INDEX idx_educational_standard_type ON educational_standard(standard_type);
CREATE INDEX idx_educational_standard_subject ON educational_standard(subject_area);
CREATE INDEX idx_educational_standard_grade ON educational_standard(grade_level);
CREATE INDEX idx_educational_standard_is_active ON educational_standard(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_standards_framework_tenant ON standards_framework(tenant_id);
CREATE INDEX idx_standards_framework_type ON standards_framework(framework_type);
CREATE INDEX idx_standards_framework_adoption ON standards_framework(adoption_status);
CREATE INDEX idx_standards_framework_is_primary ON standards_framework(is_primary_framework) WHERE is_primary_framework = TRUE;
CREATE INDEX idx_content_standard_alignment_content ON content_standard_alignment(content_id);
CREATE INDEX idx_content_standard_alignment_standard ON content_standard_alignment(standard_id);
CREATE INDEX idx_content_standard_alignment_framework ON content_standard_alignment(framework_id);
CREATE INDEX idx_content_standard_alignment_strength ON content_standard_alignment(alignment_strength);
CREATE INDEX idx_content_standard_alignment_verified ON content_standard_alignment(alignment_verified) WHERE alignment_verified = TRUE;
CREATE INDEX idx_outcome_standard_alignment_standard ON outcome_standard_alignment(standard_id);
CREATE INDEX idx_outcome_standard_alignment_framework ON outcome_standard_alignment(framework_id);
CREATE INDEX idx_outcome_standard_alignment_assessment ON outcome_standard_alignment(assessment_id);
CREATE INDEX idx_outcome_standard_alignment_verified ON outcome_standard_alignment(alignment_verified) WHERE alignment_verified = TRUE;
CREATE INDEX idx_compliance_requirement_tenant ON compliance_requirement(tenant_id);
CREATE INDEX idx_compliance_requirement_type ON compliance_requirement(requirement_type);
CREATE INDEX idx_compliance_requirement_status ON compliance_requirement(compliance_status);
CREATE INDEX idx_compliance_requirement_effective ON compliance_requirement(effective_date, expiration_date);
CREATE INDEX idx_accessibility_standard_tenant ON accessibility_standard(tenant_id);
CREATE INDEX idx_accessibility_standard_wcag ON accessibility_standard(wcag_level);
CREATE INDEX idx_accessibility_standard_is_critical ON accessibility_standard(is_critical) WHERE is_critical = TRUE;
CREATE INDEX idx_accessibility_standard_compliance ON accessibility_standard(current_compliance_percentage);
CREATE INDEX idx_data_privacy_policy_type ON data_privacy_policy(policy_type);
CREATE INDEX idx_data_privacy_policy_next_review ON data_privacy_policy(next_review_date);
CREATE INDEX idx_accreditation_metric_tenant ON accreditation_metric(tenant_id);
CREATE INDEX idx_accreditation_metric_type ON accreditation_metric(metric_type);
CREATE INDEX idx_accreditation_metric_is_met ON accreditation_metric(is_met);
CREATE INDEX idx_accreditation_metric_last_measured ON accreditation_metric(last_measured DESC);
CREATE INDEX idx_content_standard_alignment_tenant ON content_standard_alignment(tenant_id);
CREATE INDEX idx_outcome_standard_alignment_tenant ON outcome_standard_alignment(tenant_id);
CREATE INDEX idx_outcome_result_user_outcome ON outcome_result(user_id, outcome_id);
CREATE INDEX idx_accreditation_metric_responsible_user_id_fk ON accreditation_metric(responsible_user_id);
CREATE INDEX idx_compliance_requirement_responsible_user_id_fk ON compliance_requirement(responsible_user_id);
CREATE INDEX idx_content_standard_alignment_verified_by_fk ON content_standard_alignment(verified_by);
CREATE INDEX idx_data_privacy_policy_approved_by_fk ON data_privacy_policy(approved_by);
CREATE INDEX idx_data_privacy_policy_privacy_officer_id_fk ON data_privacy_policy(privacy_officer_id);
CREATE INDEX idx_educational_standard_parent_standard_id_fk ON educational_standard(parent_standard_id);
CREATE INDEX idx_outcome_result_course_id_fk ON outcome_result(course_id);
CREATE INDEX idx_outcome_result_outcome_id_fk ON outcome_result(outcome_id);
CREATE INDEX idx_outcome_result_tenant_id_fk ON outcome_result(tenant_id);
CREATE INDEX idx_outcome_standard_alignment_verified_by_fk ON outcome_standard_alignment(verified_by);
CREATE INDEX idx_outcome_proficiency_rating_course_id_fk ON outcome_proficiency_rating(course_id);
CREATE INDEX idx_outcome_proficiency_rating_org_unit_id_fk ON outcome_proficiency_rating(org_unit_id);
CREATE INDEX idx_outcome_proficiency_rating_tenant_id_fk ON outcome_proficiency_rating(tenant_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_educational_standard_date_updated BEFORE UPDATE ON educational_standard FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_standards_framework_date_updated BEFORE UPDATE ON standards_framework FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_content_standard_alignment_date_updated BEFORE UPDATE ON content_standard_alignment FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_outcome_standard_alignment_date_updated BEFORE UPDATE ON outcome_standard_alignment FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_compliance_requirement_date_updated BEFORE UPDATE ON compliance_requirement FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_accessibility_standard_date_updated BEFORE UPDATE ON accessibility_standard FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_data_privacy_policy_date_updated BEFORE UPDATE ON data_privacy_policy FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_accreditation_metric_date_updated BEFORE UPDATE ON accreditation_metric FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE educational_standard ENABLE ROW LEVEL SECURITY;
ALTER TABLE educational_standard FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_educational_standard_tenant ON educational_standard FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE standards_framework ENABLE ROW LEVEL SECURITY;
ALTER TABLE standards_framework FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_standards_framework_tenant ON standards_framework FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE content_standard_alignment ENABLE ROW LEVEL SECURITY;
ALTER TABLE content_standard_alignment FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_content_standard_alignment_tenant ON content_standard_alignment FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE outcome_standard_alignment ENABLE ROW LEVEL SECURITY;
ALTER TABLE outcome_standard_alignment FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_outcome_standard_alignment_tenant ON outcome_standard_alignment FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE compliance_requirement ENABLE ROW LEVEL SECURITY;
ALTER TABLE compliance_requirement FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_compliance_requirement_tenant ON compliance_requirement FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE accessibility_standard ENABLE ROW LEVEL SECURITY;
ALTER TABLE accessibility_standard FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_accessibility_standard_tenant ON accessibility_standard FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE data_privacy_policy ENABLE ROW LEVEL SECURITY;
ALTER TABLE data_privacy_policy FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_data_privacy_policy_tenant ON data_privacy_policy FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE accreditation_metric ENABLE ROW LEVEL SECURITY;
ALTER TABLE accreditation_metric FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_accreditation_metric_tenant ON accreditation_metric FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE outcome_result ENABLE ROW LEVEL SECURITY;
ALTER TABLE outcome_result FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_outcome_result_tenant ON outcome_result FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE outcome_proficiency_rating ENABLE ROW LEVEL SECURITY;
ALTER TABLE outcome_proficiency_rating FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_outcome_proficiency_rating_tenant ON outcome_proficiency_rating FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
