-- ================================================================================
-- 02. IDENTITY, ACCOUNTS & PROFILES (23 tables)
-- ================================================================================
-- Tables: users, user_profile, roles, permissions, role_permissions, user_role, user_session, login_history, password_reset_token, mfa_configuration, email_verification_token, account_lock, audit_log, student_profile, platform_user_profile, employee_profile, academic_advisor_profile, observer_student_link, platform_support_access, user_accessibility_settings, user_preferences, accommodation, user_login
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- Users: the single authentication entity for every account on the platform
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID DEFAULT app_current_tenant_id(),
    is_system_user BOOLEAN NOT NULL DEFAULT FALSE,
    user_type VARCHAR(30) NOT NULL,
    username VARCHAR(100) NOT NULL,
    email CITEXT NOT NULL,
    password_hash VARCHAR(255),
    account_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    date_email_verified TIMESTAMPTZ,
    auth_provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    external_auth_id VARCHAR(255),
    sis_source_id VARCHAR(255),
    mfa_required BOOLEAN NOT NULL DEFAULT FALSE,
    last_login TIMESTAMPTZ,
    login_count INT NOT NULL DEFAULT 0,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_failed_login TIMESTAMPTZ,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    account_locked_until TIMESTAMPTZ,
    password_last_changed TIMESTAMPTZ,
    password_expires_at TIMESTAMPTZ,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    last_password_reset TIMESTAMPTZ,
    is_test_user BOOLEAN NOT NULL DEFAULT FALSE,
    calendar_feed_token_hash VARCHAR(255),
    created_by UUID,
    date_deactivated TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_tenant_user UNIQUE (tenant_id, user_id),
    CONSTRAINT uk_users_user_system UNIQUE (user_id, is_system_user),
    CONSTRAINT uk_users_username_tenant UNIQUE (tenant_id, username),
    CONSTRAINT uk_users_email_tenant UNIQUE (tenant_id, email),
    CONSTRAINT ck_users_system_tenant CHECK ( (is_system_user = TRUE AND tenant_id IS NULL AND user_type = 'PLATFORM') OR (is_system_user = FALSE AND tenant_id IS NOT NULL AND user_type <> 'PLATFORM')),
    CONSTRAINT ck_users_type CHECK (user_type IN ('PLATFORM','TENANT_ADMIN','STAFF','INSTRUCTOR','TEACHING_ASSISTANT','ADVISOR','STUDENT','OBSERVER')),
    CONSTRAINT ck_users_email CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT ck_users_username_length CHECK (LENGTH(username) BETWEEN 3 AND 100),
    CONSTRAINT ck_users_account_status CHECK (account_status IN ('INVITED','PENDING_VERIFICATION','ACTIVE','SUSPENDED','LOCKED','DEACTIVATED','DELETED')),
    CONSTRAINT ck_users_auth_provider CHECK (auth_provider IN ('LOCAL','SAML','OIDC','LDAP','GOOGLE','MICROSOFT','LTI')),
    CONSTRAINT ck_users_local_password CHECK (auth_provider <> 'LOCAL' OR password_hash IS NOT NULL OR account_status IN ('INVITED','PENDING_VERIFICATION')),
    CONSTRAINT ck_users_platform_mfa CHECK (is_system_user = FALSE OR mfa_required = TRUE),
    CONSTRAINT ck_users_counters CHECK (login_count >= 0 AND failed_login_attempts >= 0)
);

-- UserProfile: personal details shared by every user type (created automatically)
CREATE TABLE user_profile (
    profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL UNIQUE,
    first_name VARCHAR(100),
    middle_name VARCHAR(100),
    last_name VARCHAR(100),
    preferred_name VARCHAR(100),
    display_name VARCHAR(200),
    pronouns VARCHAR(50),
    phone_number VARCHAR(30),
    address VARCHAR(255),
    city VARCHAR(100),
    state_region VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'en',
    avatar_file_id UUID,
    date_of_birth DATE,
    gender VARCHAR(30),
    bio TEXT,
    profile_visibility VARCHAR(20) NOT NULL DEFAULT 'TENANT',
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_user_profile_dob CHECK (date_of_birth IS NULL OR date_of_birth <= CURRENT_DATE),
    CONSTRAINT ck_user_profile_visibility CHECK (profile_visibility IN ('PUBLIC','TENANT','COURSE','PRIVATE'))
);

-- Roles: Role definitions for authorization
CREATE TABLE roles (
    role_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID DEFAULT app_current_tenant_id(),
    role_name VARCHAR(100) NOT NULL,
    role_code VARCHAR(100),
    role_level VARCHAR(20) NOT NULL DEFAULT 'TENANT',
    description TEXT,
    parent_role_id UUID,
    display_order INT DEFAULT 0,
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_roles_name_tenant UNIQUE (role_name, tenant_id),
    CONSTRAINT uk_roles_code_tenant UNIQUE (role_code, tenant_id),
    CONSTRAINT ck_roles_name_length CHECK (LENGTH(role_name) >= 3),
    CONSTRAINT ck_roles_level CHECK (role_level IN ('PLATFORM','TENANT') AND (role_level = 'TENANT' OR tenant_id IS NULL))
);

-- Permissions: Granular access control definitions
CREATE TABLE permissions (
    permission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID DEFAULT app_current_tenant_id(),
    permission_name VARCHAR(100) NOT NULL,
    permission_description TEXT,
    permission_code VARCHAR(100),
    resource_type VARCHAR(50),
    action VARCHAR(50),
    permission_scope VARCHAR(50) DEFAULT 'SYSTEM',
    is_active BOOLEAN DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_permissions_name UNIQUE (permission_name, tenant_id),
    CONSTRAINT uk_permissions_code UNIQUE (permission_code, tenant_id),
    CONSTRAINT ck_permissions_action CHECK (action IN ('CREATE','READ','UPDATE','DELETE','EXECUTE','APPROVE')),
    CONSTRAINT ck_permissions_scope CHECK (permission_scope IN ('PLATFORM','SYSTEM','TENANT','ORG_UNIT','COURSE','SECTION'))
);

-- Role Permissions: Many-to-many mapping of roles to permissions
CREATE TABLE role_permissions (
    role_permission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_role_permissions UNIQUE (role_id, permission_id)
);

-- UserRoles: Many-to-many between users and roles
CREATE TABLE user_role (
    user_role_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    role_scope VARCHAR(50) DEFAULT 'SYSTEM',
    scope_entity_id UUID,
    is_primary_role BOOLEAN DEFAULT FALSE,
    date_assigned DATE NOT NULL,
    date_assigned_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_role UNIQUE (tenant_id, user_id, role_id, scope_entity_id),
    CONSTRAINT ck_user_role_scope CHECK (role_scope IN ('PLATFORM','SYSTEM','TENANT','ORG_UNIT','COURSE','SECTION'))
);

-- User Sessions: Active login sessions with JWT tokens
CREATE TABLE user_session (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    refresh_token_hash VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    device_type VARCHAR(50),
    device_name VARCHAR(100),
    country VARCHAR(100),
    city VARCHAR(100),
    last_activity TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_expired TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT ck_user_session_expiry CHECK (date_expired > date_created)
);

-- Login History: Audit trail of login attempts
CREATE TABLE login_history (
    login_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID,
    username VARCHAR(100),
    ip_address INET,
    country VARCHAR(100),
    city VARCHAR(100),
    device_type VARCHAR(50),
    device_fingerprint VARCHAR(255),
    login_status VARCHAR(50) NOT NULL,
    login_method VARCHAR(50) NOT NULL,
    failure_reason TEXT,
    mfa_used BOOLEAN DEFAULT FALSE,
    is_suspicious BOOLEAN DEFAULT FALSE,
    risk_score INT,
    login_timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_login_history_status CHECK (login_status IN ('SUCCESS','FAILED','BLOCKED','MFA_REQUIRED')),
    CONSTRAINT ck_login_history_method CHECK (login_method IN ('PASSWORD','SSO','LDAP','OAUTH2','SAML'))
);

-- Password Reset Token: One-time tokens for password reset
CREATE TABLE password_reset_token (
    token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    request_ip INET,
    request_user_agent TEXT,
    attempt_count INT DEFAULT 0,
    is_used BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_expires TIMESTAMPTZ NOT NULL,
    date_used TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT ck_password_reset_token_expires CHECK (date_expires > date_created),
    CONSTRAINT ck_password_reset_attempts CHECK (attempt_count >= 0 AND attempt_count <= 10)
);

-- MFA Configuration: Multi-factor authentication settings
CREATE TABLE mfa_configuration (
    mfa_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL UNIQUE,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_type VARCHAR(50),
    totp_secret_encrypted VARCHAR(500),
    backup_codes JSONB,
    phone_number VARCHAR(20),
    hardware_key_id VARCHAR(255),
    last_mfa_at TIMESTAMPTZ,
    failed_mfa_attempts INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_mfa_configuration_type CHECK (mfa_type IS NULL OR mfa_type IN ('TOTP','SMS','EMAIL','PUSH','HARDWARE_KEY'))
);

-- Email Verification Token: Email verification tokens for signup
CREATE TABLE email_verification_token (
    token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL,
    email_to_verify VARCHAR(255) NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    request_ip INET,
    request_user_agent TEXT,
    expires_at TIMESTAMPTZ NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMPTZ,
    attempt_count INT DEFAULT 0,
    resend_count INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT ck_email_verification_token_expiry CHECK (expires_at > date_created),
    CONSTRAINT ck_email_verification_token_attempts CHECK (attempt_count >= 0 AND attempt_count <= 10),
    CONSTRAINT ck_email_verification_token_verified CHECK (is_verified = FALSE OR verified_at IS NOT NULL)
);

-- Account Lock: Brute force protection with automatic lockout
CREATE TABLE account_lock (
    lock_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL UNIQUE,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    lock_reason VARCHAR(255),
    failed_attempts INT NOT NULL DEFAULT 0,
    last_failed_login_ip INET,
    date_locked TIMESTAMPTZ,
    locked_until TIMESTAMPTZ,
    date_unlocked TIMESTAMPTZ,
    locked_by UUID,
    unlocked_by UUID,
    unlock_token_hash VARCHAR(255),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_account_lock_attempts CHECK (failed_attempts >= 0),
    CONSTRAINT ck_account_lock_state CHECK (is_locked = FALSE OR date_locked IS NOT NULL)
);

-- AuditLog: System changes and sensitive operations
CREATE TABLE audit_log (
    audit_log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID DEFAULT app_current_tenant_id(),
    actor_user_id UUID,
    actor_type VARCHAR(20) NOT NULL DEFAULT 'USER',
    support_access_id UUID,
    affected_user_id UUID,
    action_type VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100),
    resource_id UUID,
    action_status VARCHAR(50),
    severity VARCHAR(20) DEFAULT 'INFO',
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    request_id UUID,
    correlation_id UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_audit_log_severity CHECK (severity IN ('INFO','WARNING','CRITICAL')),
    CONSTRAINT ck_audit_log_actor_type CHECK (actor_type IN ('USER','PLATFORM_USER','IMPERSONATION','SYSTEM','INTEGRATION'))
);

-- StudentProfile: academic record for STUDENT users (merges the old user_academic_profile student fields)
CREATE TABLE student_profile (
    student_profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL UNIQUE,
    student_number VARCHAR(50),
    org_unit_id UUID,
    academic_level VARCHAR(30),
    program_name VARCHAR(150),
    major VARCHAR(100),
    minor VARCHAR(100),
    concentration VARCHAR(100),
    enrollment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    academic_standing VARCHAR(30),
    is_full_time BOOLEAN NOT NULL DEFAULT TRUE,
    cumulative_gpa NUMERIC(4,3),
    total_credits_completed NUMERIC(6,1) NOT NULL DEFAULT 0,
    total_credits_required NUMERIC(6,1),
    date_admitted DATE,
    admit_term_id UUID,
    expected_graduation_term_id UUID,
    date_graduated DATE,
    advisor_user_id UUID,
    ferpa_directory_hold BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_student_profile_number UNIQUE (tenant_id, student_number),
    CONSTRAINT ck_student_profile_level CHECK (academic_level IS NULL OR academic_level IN ('CERTIFICATE','ASSOCIATE','UNDERGRADUATE','GRADUATE','DOCTORAL','PROFESSIONAL','NON_DEGREE','CONTINUING_ED')),
    CONSTRAINT ck_student_profile_status CHECK (enrollment_status IN ('APPLICANT','ADMITTED','ACTIVE','ON_LEAVE','WITHDRAWN','SUSPENDED','GRADUATED','INACTIVE')),
    CONSTRAINT ck_student_profile_standing CHECK (academic_standing IS NULL OR academic_standing IN ('GOOD','DEANS_LIST','WARNING','PROBATION','SUSPENSION','DISMISSED')),
    CONSTRAINT ck_student_profile_gpa CHECK (cumulative_gpa IS NULL OR cumulative_gpa BETWEEN 0 AND 4.0),
    CONSTRAINT ck_student_profile_credits CHECK (total_credits_completed >= 0),
    CONSTRAINT ck_student_profile_dates CHECK (date_graduated IS NULL OR date_admitted IS NULL OR date_graduated >= date_admitted)
);

-- PlatformUserProfile: staff of the company operating the LMS (system users only)
CREATE TABLE platform_user_profile (
    platform_profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL UNIQUE,
    is_system_user BOOLEAN NOT NULL DEFAULT TRUE,
    platform_role VARCHAR(30) NOT NULL DEFAULT 'SUPPORT',
    job_title VARCHAR(150),
    department VARCHAR(100),
    employee_number VARCHAR(50),
    can_impersonate BOOLEAN NOT NULL DEFAULT FALSE,
    support_tenant_scope UUID[],
    ip_allowlist INET[],
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_last_access TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_platform_user_profile_system CHECK (is_system_user = TRUE AND tenant_id IS NULL),
    CONSTRAINT ck_platform_user_profile_role CHECK (platform_role IN ('SUPER_ADMIN','PLATFORM_ADMIN','SUPPORT','CUSTOMER_SUCCESS','BILLING','ENGINEERING','SECURITY','SALES','READ_ONLY')),
    CONSTRAINT ck_platform_user_profile_impersonate CHECK (can_impersonate = FALSE OR platform_role IN ('SUPER_ADMIN','PLATFORM_ADMIN','SUPPORT'))
);

-- EmployeeProfile: everyone the institution employs (faculty, TAs, advisors, staff, institution admins)
CREATE TABLE employee_profile (
    employee_profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL UNIQUE,
    employee_number VARCHAR(50),
    employee_type VARCHAR(30) NOT NULL,
    org_unit_id UUID,
    job_title VARCHAR(150),
    employment_type VARCHAR(30) NOT NULL DEFAULT 'FULL_TIME',
    staff_category VARCHAR(30),
    academic_rank VARCHAR(30),
    office_location VARCHAR(255),
    office_phone VARCHAR(30),
    office_hours_note TEXT,
    expertise_areas JSONB,
    degrees JSONB,
    research_interests TEXT,
    max_courses_per_term INT,
    date_hired DATE,
    date_separated DATE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_employee_profile_number UNIQUE (tenant_id, employee_number),
    CONSTRAINT ck_employee_profile_type CHECK (employee_type IN ('FACULTY','TEACHING_ASSISTANT','ACADEMIC_ADVISOR','STAFF','ADMINISTRATOR')),
    CONSTRAINT ck_employee_profile_employment CHECK (employment_type IN ('FULL_TIME','PART_TIME','ADJUNCT','GRADUATE_ASSISTANT','VISITING','CONTRACT','STUDENT_WORKER')),
    CONSTRAINT ck_employee_profile_category CHECK (staff_category IS NULL OR staff_category IN ('ADMINISTRATION','REGISTRAR','ADMISSIONS','IT','INSTRUCTIONAL_DESIGN','DISABILITY_SERVICES','LIBRARY','FINANCIAL_AID','STUDENT_AFFAIRS','ATHLETICS','OTHER')),
    CONSTRAINT ck_employee_profile_rank CHECK (academic_rank IS NULL OR academic_rank IN ('PROFESSOR','ASSOCIATE_PROFESSOR','ASSISTANT_PROFESSOR','LECTURER','SENIOR_LECTURER','INSTRUCTOR','ADJUNCT','VISITING','TEACHING_ASSISTANT','EMERITUS')),
    CONSTRAINT ck_employee_profile_rank_faculty CHECK (academic_rank IS NULL OR employee_type IN ('FACULTY','TEACHING_ASSISTANT')),
    CONSTRAINT ck_employee_profile_max_courses CHECK (max_courses_per_term IS NULL OR max_courses_per_term > 0),
    CONSTRAINT ck_employee_profile_dates CHECK (date_separated IS NULL OR date_hired IS NULL OR date_separated >= date_hired)
);

-- AcademicAdvisorProfile: advising-only details; the advisor's employee data lives in employee_profile
CREATE TABLE academic_advisor_profile (
    advisor_profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL UNIQUE,
    advisor_type VARCHAR(30) NOT NULL DEFAULT 'ACADEMIC',
    specialization VARCHAR(100),
    focus_area VARCHAR(100),
    max_advisees INT,
    accepting_new_advisees BOOLEAN NOT NULL DEFAULT TRUE,
    availability_note TEXT,
    certifications JSONB,
    years_experience INT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_academic_advisor_profile_type CHECK (advisor_type IN ('ACADEMIC','FACULTY','CAREER','FINANCIAL_AID','SUCCESS_COACH','INTERNATIONAL','ATHLETICS')),
    CONSTRAINT ck_academic_advisor_profile_max CHECK (max_advisees IS NULL OR max_advisees > 0),
    CONSTRAINT ck_academic_advisor_profile_experience CHECK (years_experience IS NULL OR years_experience >= 0)
);

-- The observer can be any user of the tenant (an OBSERVER-type user or an existing employee); no separate profile.
CREATE TABLE observer_student_link (
    observer_link_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    observer_user_id UUID NOT NULL,
    student_user_id UUID NOT NULL,
    relationship VARCHAR(30) NOT NULL DEFAULT 'PARENT',
    access_level VARCHAR(20) NOT NULL DEFAULT 'VIEW_COURSES',
    consent_record_id UUID,
    created_by UUID,
    link_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    date_linked TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_revoked TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_observer_student_link_relationship CHECK (relationship IN ('PARENT','GUARDIAN','SPONSOR','MENTOR','ATHLETIC_ADVISOR','SUCCESS_COACH','COUNSELOR','EMPLOYER','OTHER')),
    CONSTRAINT ck_observer_student_link_access CHECK (access_level IN ('VIEW_COURSES','VIEW_GRADES','VIEW_ALL')),
    CONSTRAINT uk_observer_student_link UNIQUE (observer_user_id, student_user_id),
    CONSTRAINT ck_observer_student_link_not_self CHECK (observer_user_id <> student_user_id),
    CONSTRAINT ck_observer_student_link_status CHECK (link_status IN ('PENDING','ACTIVE','REVOKED'))
);

-- PlatformSupportAccess: audited, time-boxed access of a platform user into a tenant (support / impersonation)
CREATE TABLE platform_support_access (
    support_access_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    platform_user_id UUID NOT NULL,
    impersonated_user_id UUID,
    access_reason TEXT NOT NULL,
    ticket_reference VARCHAR(100),
    approved_by UUID,
    access_status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    date_start TIMESTAMPTZ,
    date_expires TIMESTAMPTZ NOT NULL,
    date_ended TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_platform_support_access_status CHECK (access_status IN ('REQUESTED','APPROVED','ACTIVE','ENDED','DENIED','EXPIRED')),
    CONSTRAINT ck_platform_support_access_dates CHECK (date_start IS NULL OR date_expires > date_start)
);

-- UserAccessibilitySettings: Accessibility preferences and accommodations
CREATE TABLE user_accessibility_settings (
    accessibility_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL UNIQUE,
    high_contrast_enabled BOOLEAN DEFAULT FALSE,
    text_size_percentage INT DEFAULT 100,
    screen_reader_enabled BOOLEAN DEFAULT FALSE,
    dyslexia_friendly_font BOOLEAN DEFAULT FALSE,
    font_family_accessible VARCHAR(100),
    captions_enabled BOOLEAN DEFAULT TRUE,
    transcripts_enabled BOOLEAN DEFAULT TRUE,
    extended_time_on_assessments BOOLEAN DEFAULT FALSE,
    extended_time_percentage INT DEFAULT 100,
    reduced_motion_enabled BOOLEAN DEFAULT FALSE,
    color_blind_mode BOOLEAN DEFAULT FALSE,
    color_blind_type VARCHAR(50),
    keyboard_navigation_enabled BOOLEAN DEFAULT FALSE,
    disable_animations BOOLEAN DEFAULT FALSE,
    low_data_mode BOOLEAN DEFAULT FALSE,
    accessibility_notes TEXT,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_user_accessibility_settings_text_size CHECK (text_size_percentage >= 50 AND text_size_percentage <= 200),
    CONSTRAINT ck_user_accessibility_settings_extended_time CHECK (extended_time_percentage IN (100,125,150,175,200,250)),
    CONSTRAINT ck_user_accessibility_settings_color_blind CHECK (color_blind_mode = FALSE OR color_blind_type IS NOT NULL)
);

-- UserPreferences: User learning and system preferences
CREATE TABLE user_preferences (
    preference_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL UNIQUE,
    learning_style VARCHAR(50),
    theme_preference VARCHAR(50) DEFAULT 'LIGHT',
    content_density VARCHAR(50) DEFAULT 'NORMAL',
    show_instructor_tips BOOLEAN DEFAULT TRUE,
    show_learning_analytics BOOLEAN DEFAULT TRUE,
    newsletter_subscription BOOLEAN DEFAULT TRUE,
    marketing_communications BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_user_preferences_learning_style CHECK (learning_style IS NULL OR learning_style IN ('VISUAL','AUDITORY','READING_WRITING','KINESTHETIC','MULTIMODAL')),
    CONSTRAINT ck_user_preferences_theme CHECK (theme_preference IN ('LIGHT','DARK','AUTO')),
    CONSTRAINT ck_user_preferences_density CHECK (content_density IN ('COMPACT','NORMAL','SPACIOUS'))
);

-- ACCOMMODATION: formal disability-services accommodations (extra time, alternate format, etc.)
CREATE TABLE accommodation (
    accommodation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    course_id UUID,
    accommodation_type VARCHAR(50) NOT NULL,
    extended_time_percentage INT,
    details JSONB,
    approved_by UUID,
    date_approved TIMESTAMPTZ,
    date_start DATE,
    date_end DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_confidential BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_accommodation_type CHECK (accommodation_type IN ('EXTENDED_TIME','EXTENDED_DEADLINE','ALTERNATE_FORMAT','SCREEN_READER','REDUCED_DISTRACTION','EXTRA_ATTEMPT','NOTE_TAKER','OTHER')),
    CONSTRAINT ck_accommodation_extra_time CHECK (extended_time_percentage IS NULL OR extended_time_percentage BETWEEN 1 AND 500),
    CONSTRAINT ck_accommodation_dates CHECK (date_end IS NULL OR date_start IS NULL OR date_end >= date_start)
);

CREATE TABLE user_login (
    login_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL,
    unique_login VARCHAR(255) NOT NULL,
    sso_config_id UUID,
    sis_source_id VARCHAR(255),
    integration_id VARCHAR(255),
    password_hash VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_last_login TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_login UNIQUE (tenant_id, unique_login, sso_config_id)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_users_tenant ON users(tenant_id);
CREATE INDEX idx_users_account_status ON users(account_status);
CREATE INDEX idx_users_active ON users(account_status) WHERE account_status = 'ACTIVE';
CREATE INDEX idx_users_date_created ON users(date_created DESC);
CREATE INDEX idx_user_profile_name ON user_profile(first_name, last_name);
CREATE INDEX idx_user_profile_country ON user_profile(country);
CREATE INDEX idx_user_profile_timezone ON user_profile(timezone);
CREATE INDEX idx_roles_tenant ON roles(tenant_id);
CREATE INDEX idx_roles_parent ON roles(parent_role_id);
CREATE INDEX idx_roles_is_active ON roles(is_active);
CREATE INDEX idx_roles_system ON roles(is_system_role) WHERE is_system_role = TRUE;
CREATE INDEX idx_permissions_tenant ON permissions(tenant_id);
CREATE INDEX idx_permissions_code ON permissions(permission_code);
CREATE INDEX idx_permissions_resource_action ON permissions(resource_type, action);
CREATE INDEX idx_permissions_scope ON permissions(permission_scope);
CREATE INDEX idx_user_role_tenant ON user_role(tenant_id);
CREATE INDEX idx_user_role_user ON user_role(user_id);
CREATE INDEX idx_user_role_role ON user_role(role_id);
CREATE INDEX idx_user_role_scope ON user_role(role_scope, scope_entity_id);
CREATE INDEX idx_user_role_primary ON user_role(user_id) WHERE is_primary_role = TRUE;
CREATE INDEX idx_user_session_tenant ON user_session(tenant_id);
CREATE INDEX idx_user_session_user ON user_session(user_id);
CREATE INDEX idx_user_session_active ON user_session(user_id) WHERE is_active = TRUE;
CREATE INDEX idx_user_session_expires ON user_session(date_expired);
CREATE INDEX idx_user_session_last_activity ON user_session(last_activity DESC);
CREATE INDEX idx_login_history_user ON login_history(user_id);
CREATE INDEX idx_login_history_tenant_timestamp ON login_history(tenant_id, login_timestamp DESC);
CREATE INDEX idx_login_history_timestamp ON login_history(login_timestamp DESC);
CREATE INDEX idx_login_history_status ON login_history(login_status);
CREATE INDEX idx_login_history_suspicious ON login_history(is_suspicious) WHERE is_suspicious = TRUE;
CREATE INDEX idx_password_reset_token_user ON password_reset_token(user_id);
CREATE INDEX idx_password_reset_token_tenant ON password_reset_token(tenant_id);
CREATE INDEX idx_password_reset_token_expires ON password_reset_token(date_expires);
CREATE INDEX idx_password_reset_token_active ON password_reset_token(user_id) WHERE is_used = FALSE;
CREATE INDEX idx_mfa_configuration_tenant ON mfa_configuration(tenant_id);
CREATE INDEX idx_mfa_configuration_enabled ON mfa_configuration(mfa_enabled) WHERE mfa_enabled = TRUE;
CREATE INDEX idx_email_verification_token_user ON email_verification_token(user_id);
CREATE INDEX idx_email_verification_token_tenant ON email_verification_token(tenant_id);
CREATE INDEX idx_email_verification_token_expires ON email_verification_token(expires_at);
CREATE INDEX idx_email_verification_token_unverified ON email_verification_token(user_id) WHERE is_verified = FALSE;
CREATE INDEX idx_account_lock_tenant ON account_lock(tenant_id);
CREATE INDEX idx_account_lock_is_locked ON account_lock(is_locked) WHERE is_locked = TRUE;
CREATE INDEX idx_account_lock_locked_until ON account_lock(locked_until) WHERE is_locked = TRUE;
CREATE INDEX idx_audit_log_tenant ON audit_log(tenant_id);
CREATE INDEX idx_audit_log_actor ON audit_log(actor_user_id);
CREATE INDEX idx_audit_log_affected_user ON audit_log(affected_user_id);
CREATE INDEX idx_audit_log_date ON audit_log(date_created DESC);
CREATE INDEX idx_audit_log_action ON audit_log(action_type);
CREATE INDEX idx_audit_log_resource ON audit_log(resource_type, resource_id);
CREATE INDEX idx_student_profile_tenant ON student_profile(tenant_id);
CREATE INDEX idx_student_profile_major ON student_profile(major);
CREATE INDEX idx_user_accessibility_settings_tenant ON user_accessibility_settings(tenant_id);
CREATE INDEX idx_user_accessibility_settings_screen_reader ON user_accessibility_settings(screen_reader_enabled) WHERE screen_reader_enabled = TRUE;
CREATE INDEX idx_user_accessibility_settings_captions ON user_accessibility_settings(captions_enabled) WHERE captions_enabled = TRUE;
CREATE INDEX idx_user_preferences_tenant ON user_preferences(tenant_id);
CREATE INDEX idx_user_preferences_learning_style ON user_preferences(learning_style);
CREATE INDEX idx_user_preferences_theme ON user_preferences(theme_preference);
CREATE UNIQUE INDEX uk_user_role_primary ON user_role(tenant_id, user_id) WHERE is_primary_role = TRUE;
CREATE UNIQUE INDEX uk_users_platform_email ON users(email) WHERE tenant_id IS NULL;
CREATE UNIQUE INDEX uk_users_platform_username ON users(username) WHERE tenant_id IS NULL;
CREATE INDEX idx_users_tenant_type ON users(tenant_id, user_type);
CREATE INDEX idx_users_status ON users(tenant_id, account_status);
CREATE UNIQUE INDEX uk_platform_user_profile_employee ON platform_user_profile(employee_number) WHERE employee_number IS NOT NULL;
CREATE INDEX idx_platform_support_access_active ON platform_support_access(platform_user_id, tenant_id) WHERE access_status = 'ACTIVE';
CREATE UNIQUE INDEX uk_users_sis_source ON users(tenant_id, sis_source_id) WHERE sis_source_id IS NOT NULL;
CREATE INDEX idx_role_permissions_tenant ON role_permissions(tenant_id);
CREATE INDEX idx_accommodation_user ON accommodation(user_id) WHERE is_active = TRUE;
CREATE INDEX idx_accommodation_approved_by_fk ON accommodation(approved_by);
CREATE INDEX idx_accommodation_course_id_fk ON accommodation(course_id);
CREATE INDEX idx_accommodation_tenant_id_fk ON accommodation(tenant_id);
CREATE INDEX idx_account_lock_locked_by_fk ON account_lock(locked_by);
CREATE INDEX idx_account_lock_unlocked_by_fk ON account_lock(unlocked_by);
CREATE INDEX idx_role_permissions_permission_id_fk ON role_permissions(permission_id);
CREATE INDEX idx_user_role_date_assigned_by_fk ON user_role(date_assigned_by);
CREATE INDEX idx_user_login_sso_config_id_fk ON user_login(sso_config_id);
CREATE INDEX idx_user_login_user_id_fk ON user_login(user_id);
CREATE INDEX idx_audit_log_support_access_id_fk ON audit_log(support_access_id);
CREATE INDEX idx_observer_student_link_consent_record_id_fk ON observer_student_link(consent_record_id);
CREATE INDEX idx_observer_student_link_tenant_id_fk ON observer_student_link(tenant_id);
CREATE INDEX idx_observer_student_link_tenant_id_observer_user_id_fk ON observer_student_link(tenant_id, observer_user_id);
CREATE INDEX idx_observer_student_link_tenant_id_student_user_id_fk ON observer_student_link(tenant_id, student_user_id);
CREATE INDEX idx_platform_support_access_approved_by_fk ON platform_support_access(approved_by);
CREATE INDEX idx_platform_support_access_tenant_id_fk ON platform_support_access(tenant_id);
CREATE INDEX idx_platform_support_access_tenant_id_impersonated_user_id_fk ON platform_support_access(tenant_id, impersonated_user_id);
CREATE INDEX idx_student_profile_admit_term_id_fk ON student_profile(admit_term_id);
CREATE INDEX idx_student_profile_advisor_user_id_fk ON student_profile(advisor_user_id);
CREATE INDEX idx_student_profile_expected_graduation_term_id_fk ON student_profile(expected_graduation_term_id);
CREATE INDEX idx_student_profile_org_unit_id_fk ON student_profile(org_unit_id);
CREATE INDEX idx_user_profile_avatar_file_id_fk ON user_profile(avatar_file_id);
CREATE INDEX idx_user_profile_tenant_id_fk ON user_profile(tenant_id);
CREATE INDEX idx_users_created_by_fk ON users(created_by);
CREATE INDEX idx_academic_advisor_profile_tenant_id_fk ON academic_advisor_profile(tenant_id);
CREATE INDEX idx_academic_advisor_profile_tenant_id_user_id_fk ON academic_advisor_profile(tenant_id, user_id);
CREATE INDEX idx_employee_profile_org_unit_id_fk ON employee_profile(org_unit_id);
CREATE INDEX idx_observer_student_link_created_by_fk ON observer_student_link(created_by);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_users_date_updated BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_user_profile_date_updated BEFORE UPDATE ON user_profile FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_roles_date_updated BEFORE UPDATE ON roles FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_mfa_configuration_date_updated BEFORE UPDATE ON mfa_configuration FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_account_lock_date_updated BEFORE UPDATE ON account_lock FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_student_profile_date_updated BEFORE UPDATE ON student_profile FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_platform_user_profile_date_updated BEFORE UPDATE ON platform_user_profile FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_employee_profile_date_updated BEFORE UPDATE ON employee_profile FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_academic_advisor_profile_date_updated BEFORE UPDATE ON academic_advisor_profile FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_user_accessibility_settings_date_updated BEFORE UPDATE ON user_accessibility_settings FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_user_preferences_date_updated BEFORE UPDATE ON user_preferences FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_accommodation_date_updated BEFORE UPDATE ON accommodation FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_user_login_date_updated BEFORE UPDATE ON user_login FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_users_create_profiles AFTER INSERT OR UPDATE OF user_type ON users
  FOR EACH ROW EXECUTE FUNCTION users_create_profiles();

CREATE TRIGGER trg_users_lock_tenant BEFORE UPDATE OF tenant_id, is_system_user ON users
  FOR EACH ROW EXECUTE FUNCTION users_lock_tenant();

CREATE TRIGGER trg_user_role_check BEFORE INSERT OR UPDATE ON user_role
  FOR EACH ROW EXECUTE FUNCTION check_user_role();

CREATE TRIGGER trg_user_profile_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON user_profile FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

CREATE TRIGGER trg_user_role_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON user_role FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

CREATE TRIGGER trg_user_session_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON user_session FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

CREATE TRIGGER trg_login_history_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON login_history FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

CREATE TRIGGER trg_password_reset_token_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON password_reset_token FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

CREATE TRIGGER trg_mfa_configuration_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON mfa_configuration FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

CREATE TRIGGER trg_email_verification_token_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON email_verification_token FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

CREATE TRIGGER trg_account_lock_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON account_lock FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

CREATE TRIGGER trg_user_accessibility_settings_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON user_accessibility_settings FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

CREATE TRIGGER trg_user_preferences_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON user_preferences FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

CREATE TRIGGER trg_user_login_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON user_login FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE users FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_users_private ON users FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE user_profile ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_profile FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_user_profile_private ON user_profile FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE roles FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_roles_read ON roles FOR SELECT USING (tenant_id IS NULL OR tenant_id = app_current_tenant_id() OR app_is_platform_admin());
CREATE POLICY rls_roles_write ON roles FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE permissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE permissions FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_permissions_read ON permissions FOR SELECT USING (tenant_id IS NULL OR tenant_id = app_current_tenant_id() OR app_is_platform_admin());
CREATE POLICY rls_permissions_write ON permissions FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE role_permissions ENABLE ROW LEVEL SECURITY;
ALTER TABLE role_permissions FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_role_permissions_tenant ON role_permissions FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE user_role ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_role FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_user_role_private ON user_role FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE user_session ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_session FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_user_session_private ON user_session FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE login_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE login_history FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_login_history_private ON login_history FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE password_reset_token ENABLE ROW LEVEL SECURITY;
ALTER TABLE password_reset_token FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_password_reset_token_private ON password_reset_token FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE mfa_configuration ENABLE ROW LEVEL SECURITY;
ALTER TABLE mfa_configuration FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_mfa_configuration_private ON mfa_configuration FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE email_verification_token ENABLE ROW LEVEL SECURITY;
ALTER TABLE email_verification_token FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_email_verification_token_private ON email_verification_token FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE account_lock ENABLE ROW LEVEL SECURITY;
ALTER TABLE account_lock FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_account_lock_private ON account_lock FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE audit_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_log FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_audit_log_private ON audit_log FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin());
ALTER TABLE student_profile ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_profile FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_student_profile_tenant ON student_profile FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE platform_user_profile ENABLE ROW LEVEL SECURITY;
ALTER TABLE platform_user_profile FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_platform_user_profile_private ON platform_user_profile FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE employee_profile ENABLE ROW LEVEL SECURITY;
ALTER TABLE employee_profile FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_employee_profile_tenant ON employee_profile FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE academic_advisor_profile ENABLE ROW LEVEL SECURITY;
ALTER TABLE academic_advisor_profile FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_academic_advisor_profile_tenant ON academic_advisor_profile FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE observer_student_link ENABLE ROW LEVEL SECURITY;
ALTER TABLE observer_student_link FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_observer_student_link_tenant ON observer_student_link FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE platform_support_access ENABLE ROW LEVEL SECURITY;
ALTER TABLE platform_support_access FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_platform_support_access_tenant ON platform_support_access FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE user_accessibility_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_accessibility_settings FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_user_accessibility_settings_private ON user_accessibility_settings FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE user_preferences ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_preferences FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_user_preferences_private ON user_preferences FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE accommodation ENABLE ROW LEVEL SECURITY;
ALTER TABLE accommodation FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_accommodation_tenant ON accommodation FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE user_login ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_login FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_user_login_private ON user_login FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
