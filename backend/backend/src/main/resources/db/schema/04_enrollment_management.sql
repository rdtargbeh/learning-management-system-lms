-- ================================================================================
-- 04. ENROLLMENT MANAGEMENT (9 tables)
-- ================================================================================
-- Tables: enrollment, waitlist, student_group, group_members, group_leader, group_set, differentiation_tag, differentiation_tag_member, observer_pairing_code
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- Enrollment: Student course enrollments
CREATE TABLE enrollment (
    enrollment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    section_id UUID NOT NULL,
    user_id UUID NOT NULL,
    enrollment_type VARCHAR(50) DEFAULT 'STUDENT',
    observed_user_id UUID,
    enrollment_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    sis_source_id VARCHAR(255),
    enrollment_source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    limit_to_section BOOLEAN NOT NULL DEFAULT FALSE,
    date_enrolled DATE NOT NULL,
    date_started DATE,
    date_withdrawn DATE,
    date_completed DATE,
    date_last_activity TIMESTAMPTZ,
    total_activity_seconds BIGINT NOT NULL DEFAULT 0,
    is_credit_bearing BOOLEAN DEFAULT TRUE,
    is_graded BOOLEAN DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_enrollment UNIQUE (section_id, user_id),
    CONSTRAINT ck_enrollment_type CHECK (enrollment_type IN ('STUDENT', 'AUDIT', 'OBSERVER', 'VISITING', 'WITHDRAWN_STUDENT')),
    CONSTRAINT ck_enrollment_status CHECK (enrollment_status IN ('ACTIVE', 'WITHDRAWN', 'COMPLETED', 'DROPPED', 'DEFERRED', 'ON_LEAVE')),
    CONSTRAINT ck_enrollment_dates CHECK (date_withdrawn IS NULL OR date_withdrawn >= date_enrolled),
    CONSTRAINT ck_enrollment_dates2 CHECK (date_completed IS NULL OR date_completed >= date_enrolled),
    CONSTRAINT ck_enrollment_observer CHECK (enrollment_type <> 'OBSERVER' OR observed_user_id IS NOT NULL),
    CONSTRAINT ck_enrollment_source CHECK (enrollment_source IN ('MANUAL','SIS','SELF','API','LTI'))
);

-- Waitlist: Course waitlist management
CREATE TABLE waitlist (
    waitlist_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    section_id UUID NOT NULL,
    user_id UUID NOT NULL,
    position INT NOT NULL,
    waitlist_status VARCHAR(50) DEFAULT 'WAITING',
    date_added DATE NOT NULL,
    date_offered TIMESTAMPTZ,
    date_offered_response TIMESTAMPTZ,
    offer_response VARCHAR(50),
    auto_enroll_when_available BOOLEAN DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_waitlist UNIQUE (section_id, user_id),
    CONSTRAINT ck_waitlist_position CHECK (position > 0),
    CONSTRAINT ck_waitlist_status CHECK (waitlist_status IN ('WAITING', 'OFFERED', 'ACCEPTED', 'DECLINED', 'EXPIRED', 'ENROLLED'))
);

-- StudentGroup: Collaborative learning groups within courses
CREATE TABLE student_group (
    group_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    group_set_id UUID,
    section_id UUID NOT NULL,
    group_name VARCHAR(255) NOT NULL,
    group_description TEXT,
    group_type VARCHAR(50),
    max_members INT,
    current_members INT DEFAULT 0,
    date_created_group DATE NOT NULL,
    date_start DATE,
    date_end DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_student_group_name CHECK (LENGTH(group_name) >= 3),
    CONSTRAINT ck_student_group_max_members CHECK (max_members IS NULL OR max_members > 0),
    CONSTRAINT ck_student_group_current_members CHECK (current_members >= 0),
    CONSTRAINT ck_student_group_dates CHECK (date_end IS NULL OR date_end >= date_start)
);

-- GroupMembers: Membership in student groups
CREATE TABLE group_members (
    member_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMPTZ,
    is_active BOOLEAN DEFAULT TRUE,
    contribution_score INT DEFAULT 0,
    CONSTRAINT uk_group_members UNIQUE (group_id, user_id),
    CONSTRAINT ck_group_members_role CHECK (role IN ('MEMBER', 'LEADER', 'MODERATOR', 'OBSERVER')),
    CONSTRAINT ck_group_members_contribution CHECK (contribution_score >= 0 AND contribution_score <= 100)
);

-- GroupLeader: Group leadership assignments
CREATE TABLE group_leader (
    leader_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    leadership_role VARCHAR(50),
    appointed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    appointed_by UUID,
    term_end_at TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_group_leader_role CHECK (leadership_role IS NULL OR leadership_role IN ('PROJECT_LEAD', 'SCRIBE', 'PRESENTER', 'COORDINATOR', 'FACILITATOR')),
    CONSTRAINT ck_group_leader_term CHECK (term_end_at IS NULL OR term_end_at > appointed_at)
);

-- GROUP SET: course-level grouping category (Canvas group sets); student_group gets group_set_id
CREATE TABLE group_set (
    group_set_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    set_name VARCHAR(255) NOT NULL,
    allow_self_signup BOOLEAN NOT NULL DEFAULT FALSE,
    restrict_to_section BOOLEAN NOT NULL DEFAULT FALSE,
    max_members_per_group INT,
    auto_assign_leader BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_group_set_name UNIQUE (course_id, set_name),
    CONSTRAINT ck_group_set_max CHECK (max_members_per_group IS NULL OR max_members_per_group > 0)
);

CREATE TABLE differentiation_tag (
    tag_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    tag_description TEXT,
    created_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_differentiation_tag UNIQUE (course_id, tag_name)
);

CREATE TABLE differentiation_tag_member (
    tag_member_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    tag_id UUID NOT NULL,
    user_id UUID NOT NULL,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_differentiation_tag_member UNIQUE (tag_id, user_id)
);

CREATE TABLE observer_pairing_code (
    pairing_code_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    student_user_id UUID NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    date_expires TIMESTAMPTZ NOT NULL,
    used_by UUID,
    date_used TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_enrollment_section ON enrollment(section_id);
CREATE INDEX idx_enrollment_user ON enrollment(user_id);
CREATE INDEX idx_enrollment_status ON enrollment(enrollment_status);
CREATE INDEX idx_enrollment_type ON enrollment(enrollment_type);
CREATE INDEX idx_enrollment_date_enrolled ON enrollment(date_enrolled);
CREATE INDEX idx_waitlist_section ON waitlist(section_id);
CREATE INDEX idx_waitlist_user ON waitlist(user_id);
CREATE INDEX idx_waitlist_status ON waitlist(waitlist_status);
CREATE INDEX idx_waitlist_position ON waitlist(section_id, position);
CREATE INDEX idx_waitlist_date_added ON waitlist(date_added);
CREATE INDEX idx_student_group_section ON student_group(section_id);
CREATE INDEX idx_student_group_type ON student_group(group_type);
CREATE INDEX idx_student_group_is_active ON student_group(is_active);
CREATE INDEX idx_student_group_dates ON student_group(date_start, date_end);
CREATE INDEX idx_group_members_group ON group_members(group_id);
CREATE INDEX idx_group_members_user ON group_members(user_id);
CREATE INDEX idx_group_members_role ON group_members(role);
CREATE INDEX idx_group_members_is_active ON group_members(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_group_leader_group ON group_leader(group_id);
CREATE INDEX idx_group_leader_user ON group_leader(user_id);
CREATE INDEX idx_group_leader_role ON group_leader(leadership_role);
CREATE INDEX idx_group_leader_active ON group_leader(term_end_at) WHERE term_end_at IS NULL;
CREATE INDEX idx_student_group_set ON student_group(group_set_id);
CREATE INDEX idx_enrollment_tenant ON enrollment(tenant_id);
CREATE INDEX idx_waitlist_tenant ON waitlist(tenant_id);
CREATE INDEX idx_student_group_tenant ON student_group(tenant_id);
CREATE INDEX idx_group_members_tenant ON group_members(tenant_id);
CREATE INDEX idx_group_leader_tenant ON group_leader(tenant_id);
CREATE INDEX idx_enrollment_observed_user_id_fk ON enrollment(observed_user_id);
CREATE INDEX idx_group_leader_appointed_by_fk ON group_leader(appointed_by);
CREATE INDEX idx_group_set_created_by_fk ON group_set(created_by);
CREATE INDEX idx_group_set_tenant_id_fk ON group_set(tenant_id);
CREATE INDEX idx_differentiation_tag_created_by_fk ON differentiation_tag(created_by);
CREATE INDEX idx_differentiation_tag_member_tenant_id_fk ON differentiation_tag_member(tenant_id);
CREATE INDEX idx_differentiation_tag_member_user_id_fk ON differentiation_tag_member(user_id);
CREATE INDEX idx_differentiation_tag_tenant_id_fk ON differentiation_tag(tenant_id);
CREATE INDEX idx_observer_pairing_code_student_user_id_fk ON observer_pairing_code(student_user_id);
CREATE INDEX idx_observer_pairing_code_tenant_id_fk ON observer_pairing_code(tenant_id);
CREATE INDEX idx_observer_pairing_code_used_by_fk ON observer_pairing_code(used_by);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_enrollment_date_updated BEFORE UPDATE ON enrollment FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_waitlist_date_updated BEFORE UPDATE ON waitlist FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_student_group_date_updated BEFORE UPDATE ON student_group FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_group_set_date_updated BEFORE UPDATE ON group_set FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_differentiation_tag_date_updated BEFORE UPDATE ON differentiation_tag FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_waitlist_not_enrolled BEFORE INSERT OR UPDATE ON waitlist
  FOR EACH ROW EXECUTE FUNCTION waitlist_not_enrolled();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE enrollment ENABLE ROW LEVEL SECURITY;
ALTER TABLE enrollment FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_enrollment_tenant ON enrollment FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE waitlist ENABLE ROW LEVEL SECURITY;
ALTER TABLE waitlist FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_waitlist_tenant ON waitlist FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE student_group ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_group FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_student_group_tenant ON student_group FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE group_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE group_members FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_group_members_tenant ON group_members FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE group_leader ENABLE ROW LEVEL SECURITY;
ALTER TABLE group_leader FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_group_leader_tenant ON group_leader FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE group_set ENABLE ROW LEVEL SECURITY;
ALTER TABLE group_set FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_group_set_tenant ON group_set FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE differentiation_tag ENABLE ROW LEVEL SECURITY;
ALTER TABLE differentiation_tag FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_differentiation_tag_tenant ON differentiation_tag FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE differentiation_tag_member ENABLE ROW LEVEL SECURITY;
ALTER TABLE differentiation_tag_member FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_differentiation_tag_member_tenant ON differentiation_tag_member FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE observer_pairing_code ENABLE ROW LEVEL SECURITY;
ALTER TABLE observer_pairing_code FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_observer_pairing_code_tenant ON observer_pairing_code FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
