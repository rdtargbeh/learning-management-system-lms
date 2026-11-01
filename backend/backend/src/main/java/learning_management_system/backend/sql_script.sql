-- SQL Schema for the LMS Entities with Attributes and Relationships

-- Create Schema db
CREATE Schema lms_db;
USE lms_db;

CREATE SCHEMA tenant_1_schema;
CREATE SCHEMA tenant_2_schema;


-- CREATE TENANT TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Tenants (
    tenant_id BIGINT AUTO_INCREMENT PRIMARY KEY,       -- Unique identifier for the tenant
    tenant_name VARCHAR(100) NOT NULL UNIQUE,          -- Name of the tenant
    domain VARCHAR(255) NOT NULL UNIQUE,               -- Unique domain for the tenant
    schema_name VARCHAR(100) NOT NULL UNIQUE,          -- Schema name for tenant data isolation
    subscription_plan VARCHAR(50) NOT NULL,            -- Subscription plan (e.g., Basic, Premium)
    is_subscription_active BOOLEAN NOT NULL DEFAULT TRUE, -- Whether the subscription is active
    subscription_start DATE NOT NULL,                  -- Start date of the subscription
    subscription_end DATE NOT NULL,                    -- End date of the subscription
    max_users INT NOT NULL,                            -- Maximum number of users allowed
    max_courses INT NOT NULL,                          -- Maximum number of courses allowed
    storage_limit DOUBLE NOT NULL,                     -- Storage limit in GB
    region VARCHAR(100) NOT NULL,                      -- Geographical region
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',      -- Status of the tenant (e.g., ACTIVE, SUSPENDED)
    last_accessed TIMESTAMP NULL DEFAULT NULL,
    metadata TEXT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Record creation timestamp
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    created_by BIGINT NOT NULL,                        -- Foreign key to the User who created this tenant
    updated_by BIGINT,                                 -- Foreign key to the User who last updated this tenant
    admin_id BIGINT NULL,

    -- Foreign Key Constraints
    CONSTRAINT FK_TenantCreatedBy FOREIGN KEY (created_by) REFERENCES Users(user_id) ON DELETE SET NULL,
    CONSTRAINT FK_TenantUpdatedBy FOREIGN KEY (updated_by) REFERENCES Users(user_id) ON DELETE SET NULL
    CONSTRAINT FK_TenantAdmin FOREIGN KEY (admin_id) REFERENCES Users(user_id) ON DELETE SET NULL
);
CREATE INDEX idx_tenant_domain ON tenant(domain);
CREATE INDEX idx_tenant_status ON tenant(status);
CREATE INDEX idx_tenant_region ON tenant(region);




CREATE TABLE Institutions (
    institution_id BIGINT PRIMARY KEY,
    billing_address VARCHAR(100),                      -- Billing address
    payment_method VARCHAR(100),                       -- Payment method (e.g., Credit Card)
    last_payment_date DATE,                            -- Date of the last payment
    next_payment_due DATE,                             -- Date of the next payment
    contact_email VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(20),
    theme VARCHAR(50),
    logo_url VARCHAR(255),                             -- URL for the tenant's logo
    favicon_url VARCHAR(255),                          -- URL for the tenant's favicon
    login_background_image_url VARCHAR(255),           -- URL for the tenant's login background image
    time_zone VARCHAR(50) NOT NULL,
    language VARCHAR(10) DEFAULT 'en',
    support_contact VARCHAR(100),
    metadata TEXT DEFAULT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT FK_Tenant_Institution FOREIGN KEY (institution_id) REFERENCES Tenants(tenant_id) ON DELETE CASCADE
);



-- CREATE USER TABLE +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,   -- Unique identifier for the user
    username VARCHAR(50) NOT NULL UNIQUE,        -- Username for login
    email VARCHAR(50) NOT NULL UNIQUE,          -- Email address
    password VARCHAR(50) NOT NULL,              -- Securely hashed password
    first_name VARCHAR(50) NOT NULL,             -- User's first name
    last_name VARCHAR(50) NOT NULL,              -- User's last name
    profile_picture_url VARCHAR(255),            -- URL for the user's profile picture
    is_active BOOLEAN NOT NULL DEFAULT TRUE,     -- Indicates if the account is active
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE, -- Indicates if the email is verified
    failed_login_attempts INT NOT NULL DEFAULT 0, -- Tracks failed login attempts
    preferred_language VARCHAR(30              -- Preferred language (e.g., 'en', 'es')
    notification_preference VARCHAR(20),         -- Notification preference (e.g., 'EMAIL', 'SMS')
    timezone VARCHAR(50),                        -- User's time zone
    preferences TEXT,                            -- JSON for additional preferences
    metadata TEXT,                               -- JSON for custom tenant-specific data
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Account creation timestamp
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    tenant_id BIGINT,                            -- Foreign key linking to the Tenant table

    -- Foreign Key Constraints
    CONSTRAINT FK_UserTenant FOREIGN KEY (tenant_id) REFERENCES Tenants(tenant_id) ON DELETE SET NULL
);


-- CREATE A USER - ROLE RELATION TABLE   +++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE User_Roles (
    user_id BIGINT NOT NULL,                      -- Foreign key linking to User
    role_id BIGINT NOT NULL,                      -- Foreign key linking to Role
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Timestamp of role assignment

    PRIMARY KEY (user_id, role_id),               -- Composite primary key
    CONSTRAINT FK_UserRole_User FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_UserRole_Role FOREIGN KEY (role_id) REFERENCES Roles(role_id) ON DELETE CASCADE
);


CREATE TABLE User_Permission (
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, permission_id),
    CONSTRAINT FK_UserPermission_User FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_UserPermission_Permission FOREIGN KEY (permission_id) REFERENCES Permission(permission_id) ON DELETE CASCADE
);


-- CREATE ROLE TABLE  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Roles (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    role_type VARCHAR(20) NOT NULL,
    parent_role_id BIGINT,
    tenant_id BIGINT,
    priority INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    metadata TEXT,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,

    CONSTRAINT FK_ParentRole FOREIGN KEY (parent_role_id) REFERENCES Roles(role_id),
    CONSTRAINT FK_TenantRole FOREIGN KEY (tenant_id) REFERENCES Tenants(tenant_id),
    CONSTRAINT FK_CreatedBy FOREIGN KEY (created_by) REFERENCES Users(user_id),
    CONSTRAINT FK_UpdatedBy FOREIGN KEY (updated_by) REFERENCES Users(user_id)
);


-- Modify role type column
ALTER TABLE roles MODIFY COLUMN role_type ENUM(
    'GLOBAL', 'TENANT', 'SYSTEM', 'USERS', 'INSTRUCTOR', 'ADMIN', 'STUDENT', 'STAFF'
) NOT NULL;

ALTER TABLE roles MODIFY COLUMN role_type VARCHAR(20) NOT NULL;


-- CREATE PERMISSION TABLE   +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
CREATE TABLE Permissions (
    permission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    resource VARCHAR(100) NOT NULL,
    action ENUM('CREATE', 'READ', 'UPDATE', 'DELETE', 'MANAGE', 'EXECUTE') NOT NULL,
    scope VARCHAR(50) NOT NULL,  -- SYSTEM, TENANT, RESOURCE
    role_id BIGINT NOT NULL,
     group_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    conditions TEXT,  -- JSON or other custom conditions
    expires_at TIMESTAMP NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    parent_permission_id BIGINT,
    priority INT NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    metadata TEXT,  -- Custom metadata

    -- Foreign Keys
    CONSTRAINT FK_RolePermission FOREIGN KEY (role_id) REFERENCES Roles(role_id),
    CONSTRAINT FK_ParentPermission FOREIGN KEY (parent_permission_id) REFERENCES Permissions(permission_id)
);



-- CREATE A ROLE -  PERMISSION RELATIONSHIP TABLE ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Role_Permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    PRIMARY KEY (role_id, permission_id),

    -- Foreign Keys
    CONSTRAINT FK_Role FOREIGN KEY (role_id) REFERENCES Roles(role_id) ON DELETE CASCADE,
    CONSTRAINT FK_Permission FOREIGN KEY (permission_id) REFERENCES Permissions(permission_id) ON DELETE CASCADE
);



-- CREATE ACTIVITY LOG TABLE  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE ActivityLog (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,  -- Unique identifier for the activity log
    user_id BIGINT NOT NULL,                   -- Foreign key linking to the User table
    action_category ENUM('LOGIN', 'COURSE', 'USER', 'REPORT', 'OTHER') NOT NULL,  -- Broad category of the action
    action_type ENUM('LOGGED_IN', 'LOGGED_OUT', 'LOGIN_FAILED',
                     'COURSE_CREATED', 'COURSE_UPDATED', 'COURSE_DELETED',
                     'USER_REGISTERED', 'USER_UPDATED', 'USER_DEACTIVATED',
                     'REPORT_VIEWED', 'REPORT_GENERATED', 'OTHER') NOT NULL,     -- Specific type of action
    ip_address VARCHAR(45),                   -- IP address from which the action was performed
    status BOOLEAN NOT NULL,                  -- Indicates whether the action was successful (TRUE/FALSE)
    resource_type VARCHAR(100),               -- The type of resource affected (e.g., "COURSE", "USER"), if applicable
    resource_id BIGINT,                       -- The ID of the resource affected, if applicable
    metadata TEXT,                            -- Additional metadata or details about the action
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Timestamp of the action

    -- Foreign Key Constraint
    CONSTRAINT FK_UserActivity FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
);


-- CREATE NOTIFICATION TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,    -- Unique identifier
    title VARCHAR(255) NOT NULL,                          -- Notification title
    message TEXT NOT NULL,                                -- Notification message
    type ENUM('SYSTEM', 'COURSE', 'USER', 'QUIZ', 'EXAM', 'DISCUSSION', 'OTHER') NOT NULL, -- Notification type
    urgency ENUM('HIGH', 'MEDIUM', 'LOW') NOT NULL,       -- Urgency level
    delivery_method ENUM('EMAIL', 'SMS', 'IN_APP', 'PUSH') NOT NULL, -- Delivery method
    status ENUM('SENT', 'FAILED', 'PENDING') NOT NULL,    -- Notification status
    is_read BOOLEAN NOT NULL DEFAULT FALSE,              -- Whether the notification has been read
    read_at TIMESTAMP NULL,                               -- Timestamp when read
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, -- Timestamp when sent
    scheduled_at TIMESTAMP NULL,                         -- Timestamp when scheduled to be sent
    expires_at TIMESTAMP NULL,                           -- Timestamp when the notification expires
    recurrence VARCHAR(50),                              -- Recurrence pattern
    action_link VARCHAR(255),                            -- Actionable link or URL
    metadata TEXT,                                       -- Metadata for additional context
    attachments TEXT,                                    -- JSON for file attachments
    localized_message JSON,                              -- JSON for multi-language messages
    clicks INT NOT NULL DEFAULT 0,                       -- Number of clicks on the notification
    last_clicked_at TIMESTAMP NULL,                      -- Timestamp of the last click
    related_entity_id BIGINT,                            -- ID of the related entity
    related_entity_type VARCHAR(50),                    -- Type of the related entity
    user_id BIGINT NOT NULL,                             -- Recipient user
    tenant_id BIGINT,                                    -- Associated tenant
    group_id BIGINT,                                     -- Broadcast group ID
    template_id BIGINT,                                  -- ID of the notification template

    -- Foreign Key Constraints
    CONSTRAINT FK_NotificationUser FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_NotificationTenant FOREIGN KEY (tenant_id) REFERENCES Tenant(tenant_id) ON DELETE SET NULL
);


-- CREATE ADMIN TABLE  +++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Admin (
    admin_id BIGINT PRIMARY KEY,                          -- Primary key, maps to User table
    admin_level ENUM('SUPER_ADMIN', 'TENANT_ADMIN', 'DEPARTMENT_ADMIN') NOT NULL, -- Admin level
    tenant_id BIGINT,                                     -- Links to Tenant table
    department_id BIGINT,                                 -- Links to Department table
    title VARCHAR(100),                                   -- Job title or designation
    hire_date DATE,                                       -- Date of hire
    office_location VARCHAR(255),                        -- Office location
    accessibility_preferences TEXT,                      -- Accessibility preferences (e.g., screen reader)
    work_schedule TEXT,                                   -- Work schedule or availability
    preferences TEXT,                                     -- Metadata or preferences
    is_super_admin BOOLEAN NOT NULL DEFAULT FALSE,        -- Indicates system-wide privileges
    last_login TIMESTAMP NULL,                            -- Last login timestamp
    last_login_ip VARCHAR(45),                            -- Last login IP address

    -- Foreign Key Constraints
    CONSTRAINT FK_Admin_User FOREIGN KEY (admin_id) REFERENCES User(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Admin_Tenant FOREIGN KEY (tenant_id) REFERENCES Tenant(tenant_id) ON DELETE SET NULL,
    CONSTRAINT FK_Admin_Department FOREIGN KEY (department_id) REFERENCES Department(department_id) ON DELETE SET NULL
);


-- CREATE STUDENT TABLE  +++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Student (
    student_id BIGINT PRIMARY KEY, -- Matches the user_id from User
--    tenant_id BIGINT NOT NULL,     -- Direct link to Tenant
    grade_level VARCHAR(50),
    major VARCHAR(100),
    date_enrolled DATE NOT NULL,
    enrollment_status ENUM('ENROLLED', 'WITHDRAWN', 'GRADUATED') NOT NULL,
    bio TEXT DEFAULT NULL,
    overall_progress DOUBLE NOT NULL DEFAULT 0.0,
    leaderboard_points INT NOT NULL DEFAULT 0,
    attendance TEXT DEFAULT NULL,
    profile_visibility ENUM('PUBLIC', 'PRIVATE', 'TENANT_ONLY') NOT NULL DEFAULT 'TENANT_ONLY',
    graduation_date DATE DEFAULT NULL,
    custom_attributes TEXT DEFAULT NULL,
    metadata TEXT DEFAULT NULL,

    -- Foreign Key Constraints
    CONSTRAINT FK_Student_User FOREIGN KEY (student_id) REFERENCES User(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Student_Tenant FOREIGN KEY (tenant_id) REFERENCES Tenant(tenant_id) ON DELETE CASCADE
);


CREATE TABLE Student_Tenant (
    student_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    PRIMARY KEY (student_id, tenant_id),
    CONSTRAINT FK_StudentTenant_Student FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
    CONSTRAINT FK_StudentTenant_Student FOREIGN KEY (tenant_id) REFERENCES Tenant(tenant_id) ON DELETE CASCADE
);


-- Student advisors (many-to-many relationship between Student and Staff)
CREATE TABLE StudentAdvisors (
    student_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    PRIMARY KEY (student_id, staff_id),
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id) ON DELETE CASCADE
);



-- CREATE ENROLLMENT TABLE  +++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Enrollments (
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrollment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status ENUM('ACTIVE', 'COMPLETED', 'DROPPED', 'WITHDRAWN') NOT NULL DEFAULT 'ACTIVE',
    grade DOUBLE DEFAULT NULL,
    attendance_percentage DOUBLE DEFAULT NULL,
    completion_date DATE DEFAULT NULL,
    metadata TEXT DEFAULT NULL,

    -- Composite Primary Key
    PRIMARY KEY (student_id, course_id),

    -- Foreign Key Constraints
    CONSTRAINT FK_Enrollment_Student FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
    CONSTRAINT FK_Enrollment_Course FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE
);


-- CREATE STUDENT GROUP TABLE ++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Student_Group (
    group_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT DEFAULT NULL,
    course_id BIGINT NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    metadata TEXT DEFAULT NULL,

    -- Foreign Key Constraints
    CONSTRAINT FK_StudentGroup_Course FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE
);

CREATE TABLE Group_Members (
    group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (group_id, student_id),
    FOREIGN KEY (group_id) REFERENCES StudentGroup(group_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE
);

CREATE TABLE Group_Leaders (
    group_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (group_id, student_id),
    FOREIGN KEY (group_id) REFERENCES StudentGroup(group_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE
);



-- CREATE STAFF TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Staff (
    staff_id BIGINT PRIMARY KEY,
    staff_type ENUM('TEACHER', 'SUPPORT_STAFF') NOT NULL,
    title VARCHAR(100),
    subject_specialization VARCHAR(255),
    date_hired DATE NOT NULL,
    employment_status ENUM('FULL_TIME', 'PART_TIME') NOT NULL,
    office_location VARCHAR(255),
    accessibility_preferences TEXT,
    work_schedule TEXT,
    department_id BIGINT,
    tenant_id BIGINT,
    skills TEXT,
    current_workload INT,
    max_workload INT,
    availability_status ENUM('AVAILABLE', 'BUSY', 'ON_LEAVE') NOT NULL,
    engagement_status ENUM('ACTIVE', 'INACTIVE', 'RETIRED') NOT NULL,
    external_reference_id VARCHAR(255),
    metadata TEXT,
    last_updated_by BIGINT,
    last_updated_at TIMESTAMP,

    CONSTRAINT FK_Staff_User FOREIGN KEY (staff_id) REFERENCES User(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Staff_Department FOREIGN KEY (department_id) REFERENCES Department(department_id) ON DELETE SET NULL,
    CONSTRAINT FK_Staff_Tenant FOREIGN KEY (tenant_id) REFERENCES Tenant(tenant_id) ON DELETE SET NULL
);

CREATE TABLE Staff_Tenant (
    staff_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    PRIMARY KEY (staff_id, tenant_id),
    CONSTRAINT FK_StaffTenant_Staff FOREIGN KEY (staff_id) REFERENCES Staff(staff_id) ON DELETE CASCADE,
    CONSTRAINT FK_StaffTenant_Tenant FOREIGN KEY (tenant_id) REFERENCES Tenant(tenant_id) ON DELETE CASCADE
);

CREATE TABLE course_staff (
    course_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    PRIMARY KEY (course_id, staff_id),
    CONSTRAINT FK_CourseStaff_Course FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE,
    CONSTRAINT FK_CourseStaff_Staff FOREIGN KEY (staff_id) REFERENCES Staff(staff_id) ON DELETE CASCADE
);


-- CREATE COURSE TABLE  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Course (
    course_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(50) NOT NULL UNIQUE,
    course_title VARCHAR(255) NOT NULL,
    course_description TEXT,
    course_status ENUM('ACTIVE', 'ARCHIVED', 'COMPLETED') NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    tenant_id BIGINT NOT NULL,
    department_id BIGINT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    enrollment_start DATE NOT NULL,
    enrollment_end DATE NOT NULL,
    max_enrollments INT DEFAULT NULL,
    visibility ENUM('PUBLIC', 'PRIVATE', 'TENANT_ONLY') NOT NULL DEFAULT 'PRIVATE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    metadata TEXT,

    CONSTRAINT FK_Course_Tenant FOREIGN KEY (tenant_id) REFERENCES Tenant(tenant_id) ON DELETE CASCADE,
    CONSTRAINT FK_Course_Department FOREIGN KEY (department_id) REFERENCES Department(department_id) ON DELETE SET NULL,
    CONSTRAINT FK_Course_CreatedBy FOREIGN KEY (created_by) REFERENCES User(user_id) ON DELETE CASCADE
);


CREATE TABLE course_tags (
    course_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (course_id, tag),
    CONSTRAINT FK_CourseTags_Course FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE
);


-- CREATE COURSE MODULE TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
--  FINAL
CREATE TABLE Course_Module (
    module_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    `order` INT NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    parent_module_id BIGINT,
    release_date DATE,
    release_condition TEXT,
    objectives TEXT,
    points INT,
    badge_url VARCHAR(255),
    average_completion_time INT,
    engagement_score DOUBLE,
    prerequisites TEXT,
    course_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    metadata TEXT,

    CONSTRAINT FK_Module_Course FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE,
    CONSTRAINT FK_Module_User FOREIGN KEY (created_by) REFERENCES User(user_id) ON DELETE SET NULL,
    CONSTRAINT FK_Module_Parent FOREIGN KEY (parent_module_id) REFERENCES Module(module_id) ON DELETE SET NULL
);


-- CREATE MODULE CONTENT TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE ModuleContent (
    content_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type ENUM('VIDEO', 'PDF', 'LINK', 'QUIZ') NOT NULL,
    url VARCHAR(255) NOT NULL,
    `order` INT NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    completion_criteria TEXT,
    engagement_score DOUBLE,
    module_id BIGINT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    previous_version_id BIGINT,
    language VARCHAR(10) DEFAULT 'en',
    access_type ENUM('PUBLIC', 'ENROLLED_ONLY', 'TEACHER_ONLY') NOT NULL DEFAULT 'ENROLLED_ONLY',
    availability_start DATE,
    availability_end DATE,
    interactivity_level ENUM('NONE', 'QUIZ', 'DISCUSSION') DEFAULT 'NONE',
    prerequisite_content_id BIGINT,
    analytics TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    metadata TEXT,

    -- Foreign Key Constraints
    CONSTRAINT FK_Content_Module FOREIGN KEY (module_id) REFERENCES Module(module_id) ON DELETE CASCADE,
    CONSTRAINT FK_Content_PreviousVersion FOREIGN KEY (previous_version_id) REFERENCES Content(content_id) ON DELETE SET NULL,
    CONSTRAINT FK_Content_Prerequisite FOREIGN KEY (prerequisite_content_id) REFERENCES Content(content_id) ON DELETE SET NULL
);


CREATE TABLE Content_Tags (
    content_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (content_id, tag),
    CONSTRAINT FK_ContentTags_Content FOREIGN KEY (content_id) REFERENCES Content(content_id) ON DELETE CASCADE
);



-- CREATE ATTACHMENT TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Attachment (
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_url VARCHAR(255) NOT NULL,
    storage_type VARCHAR(20) NOT NULL,
    external_resource_id VARCHAR(255),
    linked_entity_type VARCHAR(50) NOT NULL,
    linked_entity_id BIGINT NOT NULL,
    access_level ENUM('STUDENT', 'TEACHER', 'ADMIN') NOT NULL,
    visibility ENUM('PUBLIC', 'ENROLLED_ONLY', 'TEACHER_ONLY') NOT NULL DEFAULT 'ENROLLED_ONLY',
    category ENUM('LECTURE_MATERIAL', 'QUIZ_ATTACHMENT', 'ANNOUNCEMENT_ATTACHMENT') NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by BIGINT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    previous_version_id BIGINT,
    is_compressed BOOLEAN NOT NULL DEFAULT FALSE,
    compression_type VARCHAR(20),
    download_count INT DEFAULT 0,
    last_accessed TIMESTAMP,
    thumbnail_url VARCHAR(255),
    metadata TEXT,

    CONSTRAINT FK_Attachment_User FOREIGN KEY (uploaded_by) REFERENCES User(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Attachment_PreviousVersion FOREIGN KEY (previous_version_id) REFERENCES Attachment(attachment_id) ON DELETE SET NULL
);


-- CREATE ASSIGNMENT TABLE  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Assignment (
    assignment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATETIME NOT NULL,
    available_from DATETIME,
    available_until DATETIME,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    enable_peer_reviews BOOLEAN NOT NULL DEFAULT FALSE,
    completion_rate DOUBLE DEFAULT NULL,
    average_grade DOUBLE DEFAULT NULL,
    points DOUBLE NOT NULL,
    submission_type ENUM('FILE_UPLOAD', 'TEXT', 'QUIZ', 'NONE') NOT NULL,
    assignment_type ENUM('ESSAY', 'PROJECT', 'QUIZ', 'HOMEWORK', 'OTHER') NOT NULL,
    max_attempts INT NOT NULL DEFAULT 1,
    grace_period_minutes INT NOT NULL DEFAULT 0,
    visibility ENUM('PUBLIC', 'ENROLLED_ONLY') NOT NULL DEFAULT 'ENROLLED_ONLY',
    course_id BIGINT NOT NULL,
    module_id BIGINT DEFAULT NULL,
    student_group_id BIGINT DEFAULT NULL,
    instructions TEXT,
    status ENUM('DRAFT', 'PUBLISHED', 'COMPLETED') NOT NULL DEFAULT 'DRAFT',
    grading_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT NOT NULL,
    metadata TEXT,
    grading_id BIGINT DEFAULT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT FK_Assignment_Course FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE,
    CONSTRAINT FK_Assignment_Module FOREIGN KEY (module_id) REFERENCES Module(module_id) ON DELETE SET NULL,
    CONSTRAINT FK_Assignment_StudentGroup FOREIGN KEY (student_group_id) REFERENCES StudentGroup(group_id) ON DELETE SET NULL,
    CONSTRAINT FK_Assignment_User FOREIGN KEY (created_by) REFERENCES User(user_id) ON DELETE CASCADE,
    FOREIGN KEY (grading_id) REFERENCES Grading(grading_id) ON DELETE SET NULL
);



-- CREATE COMMENT TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Comment (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    content_processed TEXT DEFAULT NULL, -- For pre-rendered content (optional)
    author_id BIGINT DEFAULT NULL, -- Nullable for anonymous comments
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    parent_comment_id BIGINT DEFAULT NULL, -- For nested replies
    linked_entity_type VARCHAR(50) NOT NULL, -- E.g., ANNOUNCEMENT, DISCUSSION
    linked_entity_id BIGINT NOT NULL, -- ID of the linked entity
    status ENUM('APPROVED', 'FLAGGED', 'HIDDEN') NOT NULL DEFAULT 'APPROVED',
    last_reply_date TIMESTAMP DEFAULT NULL,
    number_of_replies INT NOT NULL DEFAULT 0,
    mention_users TEXT DEFAULT NULL, -- JSON field for mentioned users
    visibility ENUM('PUBLIC', 'ROLE_BASED', 'GROUP_ONLY', 'PRIVATE') NOT NULL DEFAULT 'PUBLIC',
    group_id BIGINT DEFAULT NULL, -- For restricting visibility to a student group
    reactions INT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    edit_history TEXT DEFAULT NULL, -- JSON field for edit history
    date_deleted TIMESTAMP DEFAULT NULL,
    category ENUM('QUESTION', 'FEEDBACK', 'DISCUSSION', 'OTHER') DEFAULT NULL,
    language VARCHAR(20) DEFAULT 'en',
    flagged_count INT NOT NULL DEFAULT 0,
    date_archived TIMESTAMP DEFAULT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    metadata TEXT DEFAULT NULL, -- For extensibility and custom attributes

    -- Foreign Key Constraints
    CONSTRAINT FK_Comment_User FOREIGN KEY (author_id) REFERENCES User(user_id) ON DELETE SET NULL,
    CONSTRAINT FK_Comment_Parent FOREIGN KEY (parent_comment_id) REFERENCES Comment(comment_id) ON DELETE CASCADE,
    CONSTRAINT FK_Comment_Group FOREIGN KEY (group_id) REFERENCES StudentGroup(group_id) ON DELETE SET NULL
);



-- CREATE SUBMISSION TABLE  +++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Submission (
    submission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT,
    assessment_id BIGINT,
    user_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    time_taken BIGINT,
    marks_obtained DOUBLE,
    status ENUM('SUBMITTED', 'PENDING', 'GRADED', 'RESUBMITTED') NOT NULL DEFAULT 'SUBMITTED',
    grading_status ENUM('COMPLETED', 'PENDING', 'GRADED', 'IN_PROGRESS') NOT NULL DEFAULT 'PENDING',
    feedback TEXT,
    rubric_details TEXT,
    grade_comments TEXT,
    grade_level VARCHAR(50),
    time_per_question TEXT,
    score_distribution TEXT,
    plagiarism_score DOUBLE,
    analysis_report_url VARCHAR(255),
    is_flagged_for_plagiarism BOOLEAN NOT NULL DEFAULT FALSE,
    attempt_number INT NOT NULL DEFAULT 1,
    retry_count INT NOT NULL DEFAULT 0,
    auto_graded BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_content TEXT,
    metadata TEXT,
    is_finalized BOOLEAN NOT NULL DEFAULT FALSE,
    is_reviewed BOOLEAN NOT NULL DEFAULT FALSE,
    submission_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    proctoring_session_id BIGINT,

     -- Foreign Key Constraints
    CONSTRAINT fk_submission_assignment FOREIGN KEY (assignment_id) REFERENCES Assignment (assignment_id) ON DELETE SET NULL,
    CONSTRAINT fk_submission_assessment FOREIGN KEY (assessment_id) REFERENCES Assessment (assessment_id) ON DELETE SET NULL,
    CONSTRAINT FK_Submission_Student FOREIGN KEY (student_id) REFERENCES User(user_id) ON DELETE CASCADE
    CONSTRAINT fk_submission_proctoring FOREIGN KEY (proctoring_session_id) REFERENCES ProctoringSession (proctoring_session_id) ON DELETE SET NULL
);

ALTER TABLE Submission ADD CONSTRAINT unique_student_assessment UNIQUE (student_id, assessment_id);



-- CREATE ANNOUNCEMENT TABLE  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Announcement (
    announcement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    rich_text_content TEXT DEFAULT NULL,
    category VARCHAR(50) DEFAULT NULL,
    target_roles TEXT DEFAULT NULL,
    target_entities TEXT DEFAULT NULL,
    visibility ENUM('PUBLIC', 'COURSE_ONLY', 'PRIVATE') NOT NULL,
    priority ENUM('HIGH', 'MEDIUM', 'LOW') DEFAULT NULL,
    action_link VARCHAR(255) DEFAULT NULL,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_draft BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    view_count int,
    replyCount int,
    recurrence_pattern VARCHAR(50) DEFAULT NULL,
    tags TEXT DEFAULT NULL,
    scheduled_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT FK_Announcement_User FOREIGN KEY (created_by) REFERENCES User(user_id) ON DELETE CASCADE
);


CREATE TABLE Announcement_Attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    announcement_id BIGINT NOT NULL,
    attachment_id BIGINT NOT NULL,

    -- Foreign Key Constraints
    CONSTRAINT FK_Announcement_Attachment_Announcement FOREIGN KEY (announcement_id) REFERENCES Announcement(announcement_id) ON DELETE CASCADE,
    CONSTRAINT FK_Announcement_Attachment_Attachment FOREIGN KEY (attachment_id) REFERENCES Attachment(attachment_id) ON DELETE CASCADE
);

CREATE TABLE Announcement_Comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    announcement_id BIGINT NOT NULL,
    comment_id BIGINT NOT NULL,

    -- Foreign Key Constraints
    CONSTRAINT FK_Announcement_Comment_Announcement FOREIGN KEY (announcement_id) REFERENCES Announcement(announcement_id) ON DELETE CASCADE,
    CONSTRAINT FK_Announcement_Comment_Comment FOREIGN KEY (comment_id) REFERENCES Comment(comment_id) ON DELETE CASCADE
);

CREATE TABLE Announcement_Views (
    announcement_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    PRIMARY KEY (announcement_id, user_id),

    -- Foreign Key Constraints
    CONSTRAINT FK_Announcement_Views_Announcement FOREIGN KEY (announcement_id) REFERENCES Announcement(announcement_id) ON DELETE CASCADE,
    CONSTRAINT FK_Announcement_Views_User FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
);

-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++



--  CREATE ASSESSMENT TABLE  +++++++++++++++++++++++++++++++++++++++++++++++++++++
--  FINAL  |  Exam and Quiz
CREATE TABLE Assessment (
    assessment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT DEFAULT NULL,
    type ENUM('EXAM', 'QUIZ') NOT NULL,
    course_id BIGINT NOT NULL,
    shuffle_questions BOOLEAN NOT NULL DEFAULT FALSE,
    question_display_mode ENUM('ALL_AT_ONCE', 'ONE_AT_A_TIME') DEFAULT 'ALL_AT_ONCE',
    time_limit INT DEFAULT NULL,
    time_remaining_warning INT DEFAULT NULL,
    allowed_attempts INT NOT NULL DEFAULT 1,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    is_auto_graded BOOLEAN NOT NULL DEFAULT FALSE,
    show_correct_answers BOOLEAN NOT NULL DEFAULT TRUE,
    feedback_policy ENUM('IMMEDIATE', 'AFTER_ATTEMPT', 'AFTER_CLOSE', 'NEVER') DEFAULT 'IMMEDIATE',
    requires_password BOOLEAN NOT NULL DEFAULT FALSE,
    password VARCHAR(255) DEFAULT NULL,
    is_proctored BOOLEAN NOT NULL DEFAULT FALSE,
    question_navigation ENUM('FREE', 'RESTRICTED') DEFAULT 'FREE',
    randomize_questions BOOLEAN NOT NULL DEFAULT FALSE,
    metadata TEXT DEFAULT NULL,
    allow_retake_on_failure BOOLEAN NOT NULL DEFAULT FALSE,
    grading_id BIGINT DEFAULT NULL,
    created_by BIGINT NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT FK_Assessment_Course FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE,
    CONSTRAINT FK_Assessment_User FOREIGN KEY (created_by) REFERENCES User(user_id) ON DELETE CASCADE,
    FOREIGN KEY (grading_id) REFERENCES Grading(grading_id) ON DELETE SET NULL
);



-- CREATE QUESTION TABLE  +++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Question (
    question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_text TEXT NOT NULL,
    question_type ENUM('MCQ', 'TRUE_FALSE', 'ESSAY', 'MATCHING', 'NUMERIC_RESPONSE', 'FILE_UPLOAD') NOT NULL,
    question_options TEXT DEFAULT NULL,
    correct_answer TEXT DEFAULT NULL,
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT NULL,
    tags TEXT DEFAULT NULL,
    explanation TEXT DEFAULT NULL,
    feedback TEXT DEFAULT NULL,
    media TEXT DEFAULT NULL,
    is_randomized BOOLEAN NOT NULL DEFAULT FALSE,
    show_correct_answers BOOLEAN NOT NULL DEFAULT FALSE,
    time_limit INT DEFAULT NULL,
    metadata TEXT DEFAULT NULL,
    assessment_id BIGINT DEFAULT NULL,
    question_bank_id BIGINT DEFAULT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT FK_Question_Assessment FOREIGN KEY (assessment_id) REFERENCES Assessment(assessment_id) ON DELETE SET NULL,
    CONSTRAINT FK_Question_QuestionBank FOREIGN KEY (question_bank_id) REFERENCES QuestionBank(question_bank_id) ON DELETE SET NULL
);



-- CREATE QUESTION BANK TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++
--  FINAL
CREATE TABLE QuestionBank (
    question_bank_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT DEFAULT NULL,
    tags TEXT DEFAULT NULL,
    type ENUM('PRIVATE', 'SHARED', 'PUBLIC') NOT NULL DEFAULT 'PRIVATE',
    parent_bank_id BIGINT DEFAULT NULL,
    version INT NOT NULL DEFAULT 1,
    created_by BIGINT NOT NULL,
    shared_with TEXT DEFAULT NULL,
    linked_assessments TEXT DEFAULT NULL,
    statistics TEXT DEFAULT NULL,
    access_logs TEXT DEFAULT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT FK_QuestionBank_Parent FOREIGN KEY (parent_bank_id) REFERENCES QuestionBank(question_bank_id) ON DELETE SET NULL,
    CONSTRAINT FK_QuestionBank_User FOREIGN KEY (created_by) REFERENCES User(user_id) ON DELETE CASCADE
);

-- CREATE QUESTION MARKS TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++
--  FINAL
CREATE TABLE QuestionMarks (
    grading_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    marks INT NOT NULL,
    partial_marks INT,
    rubric TEXT,
    adaptive_grading BOOLEAN NOT NULL DEFAULT FALSE,
    average_score DOUBLE,
    total_attempts BIGINT,
    total_marks_earned BIGINT,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (grading_id, question_id),
    CONSTRAINT fk_grading FOREIGN KEY (grading_id) REFERENCES Grading(grading_id) ON DELETE CASCADE,
    CONSTRAINT fk_question FOREIGN KEY (question_id) REFERENCES Question(question_id) ON DELETE CASCADE
);


-- CREATE DISCUSSION TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Discussion (
    discussion_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT DEFAULT NULL,
    course_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    parent_discussion_id BIGINT DEFAULT NULL,
    category VARCHAR(50) DEFAULT NULL,
    status ENUM('OPEN', 'CLOSED', 'ARCHIVED') NOT NULL DEFAULT 'OPEN',
    visibility ENUM('PUBLIC', 'COURSE_ONLY', 'PRIVATE') NOT NULL,
    tags TEXT DEFAULT NULL,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    is_anonymous_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    allow_replies BOOLEAN NOT NULL DEFAULT TRUE,
    comment_count INT NOT NULL DEFAULT 0,
    moderators TEXT DEFAULT NULL,
    settings TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT FK_Discussion_Course FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE,
    CONSTRAINT FK_Discussion_Author FOREIGN KEY (author_id) REFERENCES User(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Discussion_Parent FOREIGN KEY (parent_discussion_id) REFERENCES Discussion(discussion_id) ON DELETE SET NULL
);

CREATE TABLE discussion_mentioned_users (
    discussion_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (discussion_id, user_id),
    FOREIGN KEY (discussion_id) REFERENCES discussion(discussion_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);


-- CREATE DISCUSSION ANALYTICS TABLE ++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE DiscussionAnalytics (
    analytics_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    discussion_id BIGINT NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    reply_count INT NOT NULL DEFAULT 0,
    unique_participants INT NOT NULL DEFAULT 0,
    last_activity TIMESTAMP DEFAULT NULL,
    average_response_time DOUBLE NULL,
    most_active_participant_id BIGINT NULL,
    engagement_details TEXT DEFAULT NULL,
    sentiment_score DOUBLE NULL,

    -- Foreign Key Constraints
    CONSTRAINT FK_DiscussionAnalytics_Discussion FOREIGN KEY (discussion_id) REFERENCES Discussion(discussion_id) ON DELETE CASCADE
);


-- CREATE BADGE TABLE  ++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Badge (
    badge_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT DEFAULT NULL,
    icon_url VARCHAR(255) DEFAULT NULL,
    criteria TEXT DEFAULT NULL,
    tenant_id BIGINT DEFAULT NULL,
    created_by BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    metadata TEXT DEFAULT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT FK_Badge_Tenant FOREIGN KEY (tenant_id) REFERENCES Tenant(tenant_id) ON DELETE SET NULL,
    CONSTRAINT FK_Badge_Creator FOREIGN KEY (created_by) REFERENCES User(user_id) ON DELETE CASCADE
);

-- Student badges (many-to-many relationship between Student and Badge)
CREATE TABLE StudentBadges  (
    student_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    date_awarded TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, badge_id),
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES Badge(badge_id) ON DELETE CASCADE
);



-- CREATE FEEDBACK TABLE  +++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Feedback (
    feedback_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    linked_entity_id BIGINT NOT NULL,
    linked_entity_type VARCHAR(50) NOT NULL,
    feedback_text TEXT DEFAULT NULL,
    feedback_type ENUM('TEXT', 'RUBRIC', 'AUDIO', 'VIDEO') NOT NULL,
    parent_feedback_id BIGINT DEFAULT NULL,
    visibility ENUM('PRIVATE', 'PUBLIC', 'RECIPIENT_ONLY') NOT NULL DEFAULT 'PRIVATE',
    anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    is_peer_review BOOLEAN NOT NULL DEFAULT FALSE,
    creator_id BIGINT NOT NULL,
    recipient_id BIGINT DEFAULT NULL,
    rubric_criteria TEXT DEFAULT NULL,
    rating DOUBLE DEFAULT NULL,
    metadata TEXT DEFAULT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'RESOLVED') NOT NULL DEFAULT 'DRAFT',
    date_read TIMESTAMP DEFAULT NULL,
    date_responded TIMESTAMP DEFAULT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT FK_Feedback_Creator FOREIGN KEY (creator_id) REFERENCES User(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Feedback_Recipient FOREIGN KEY (recipient_id) REFERENCES User(user_id) ON DELETE SET NULL,
    CONSTRAINT FK_Feedback_Parent FOREIGN KEY (parent_feedback_id) REFERENCES Feedback(feedback_id) ON DELETE SET NULL
);

-- Indexes for optimization
CREATE INDEX idx_feedback_linked_entity ON feedback (linked_entity_id, linked_entity_type);
CREATE INDEX idx_feedback_status ON feedback (status);
CREATE INDEX idx_feedback_visibility ON feedback (visibility);
CREATE INDEX idx_feedback_creator ON feedback (creator_id);

CREATE TABLE feedback_tags (
    feedback_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (feedback_id, tag),
    CONSTRAINT fk_feedback_tags_feedback FOREIGN KEY (feedback_id) REFERENCES feedback(feedback_id) ON DELETE CASCADE
);





-- CREATE EBOOK TABLE  +++++++++++++++++++++++++++++++++++++++++++++++++
CREATE TABLE Ebook (
    ebook_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    description TEXT DEFAULT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    is_interactive BOOLEAN NOT NULL DEFAULT FALSE,
    course_id BIGINT NOT NULL,
    tags TEXT DEFAULT NULL,
    metadata TEXT DEFAULT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraint
    CONSTRAINT FK_Ebook_Course FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE
);


CREATE TABLE EbookChapter (
    chapter_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ebook_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    chapter_number INT NOT NULL,
    content_path VARCHAR(500) NOT NULL,
    summary TEXT DEFAULT NULL,
    is_sequential BOOLEAN NOT NULL DEFAULT TRUE,
    has_assessment BOOLEAN NOT NULL DEFAULT FALSE,
    estimated_read_time INT DEFAULT NULL,
    metadata TEXT DEFAULT NULL,

    -- Foreign Key Constraint
    CONSTRAINT FK_EbookChapter_Ebook FOREIGN KEY (ebook_id) REFERENCES Ebook(ebook_id) ON DELETE CASCADE
);

CREATE TABLE EbookChapterAssessment (
    assessment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    questions TEXT DEFAULT NULL,
    passing_score DOUBLE NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    max_attempts INT DEFAULT NULL,
    time_limit_minutes INT DEFAULT NULL,
    metadata TEXT DEFAULT NULL,

    -- Foreign Key Constraint
    CONSTRAINT FK_EbookChapterAssessment_Chapter FOREIGN KEY (chapter_id) REFERENCES EbookChapter(chapter_id) ON DELETE CASCADE
);

CREATE TABLE EbookStudentChapterProgress (
    progress_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    chapter_id BIGINT NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completion_date TIMESTAMP DEFAULT NULL,
    assessment_score DOUBLE DEFAULT NULL,
    attempts_made INT DEFAULT 0,
    is_unlocked BOOLEAN NOT NULL DEFAULT FALSE,
    time_spent_seconds BIGINT DEFAULT 0,
    metadata TEXT DEFAULT NULL,

    -- Foreign Key Constraints
    CONSTRAINT FK_StudentChapterProgress_Student FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE,
    CONSTRAINT FK_StudentChapterProgress_Chapter FOREIGN KEY (chapter_id) REFERENCES EbookChapter(chapter_id) ON DELETE CASCADE
);


CREATE TABLE EbookDiscussion (
    discussion_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT DEFAULT NULL,
    ebook_id BIGINT DEFAULT NULL,
    chapter_id BIGINT DEFAULT NULL,
    created_by BIGINT NOT NULL,
    tags TEXT DEFAULT NULL,
    metadata TEXT DEFAULT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT FK_EbookDiscussion_Ebook FOREIGN KEY (ebook_id) REFERENCES Ebook(ebook_id) ON DELETE SET NULL,
    CONSTRAINT FK_EbookDiscussion_Chapter FOREIGN KEY (chapter_id) REFERENCES EbookChapter(chapter_id) ON DELETE SET NULL,
    CONSTRAINT FK_EbookDiscussion_User FOREIGN KEY (created_by) REFERENCES User(user_id) ON DELETE CASCADE
);


CREATE TABLE EbookAnnotation (
    annotation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ebook_id BIGINT DEFAULT NULL,
    chapter_id BIGINT DEFAULT NULL,
    highlight_range TEXT DEFAULT NULL,
    annotation_type ENUM('NOTE', 'HIGHLIGHT') NOT NULL,
    text TEXT DEFAULT NULL,
    metadata TEXT DEFAULT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT FK_EbookAnnotation_User FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_EbookAnnotation_Ebook FOREIGN KEY (ebook_id) REFERENCES Ebook(ebook_id) ON DELETE SET NULL,
    CONSTRAINT FK_EbookAnnotation_Chapter FOREIGN KEY (chapter_id) REFERENCES EbookChapter(chapter_id) ON DELETE SET NULL
);


-- CREATE GRADING TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Grading (
    grading_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grading_policy ENUM('AUTO', 'MANUAL') NOT NULL,
    total_marks INT NOT NULL,
    grading_rubric TEXT DEFAULT NULL,
    attempt_penalty DOUBLE DEFAULT NULL,
    late_submission_policy ENUM('STRICT', 'FLEXIBLE', 'NONE') DEFAULT NULL,
    late_submission_penalty_percentage DOUBLE DEFAULT NULL,
    grading_tiers TEXT DEFAULT NULL,
    assessment_id BIGINT DEFAULT NULL,
    assignment_id BIGINT DEFAULT NULL,
    question_id BIGINT DEFAULT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key Constraints
    CONSTRAINT FK_Grading_Assessment FOREIGN KEY (assessment_id) REFERENCES Assessment(assessment_id) ON DELETE CASCADE,
    CONSTRAINT FK_Grading_Assignment FOREIGN KEY (assignment_id) REFERENCES Assignment(assignment_id) ON DELETE CASCADE
    CONSTRAINT FK_Grading_Question FOREIGN KEY (question_id) REFERENCES Question(question_id) ON DELETE CASCADE
);

CREATE TABLE QuestionMarks (
    grading_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    marks INT NOT NULL,
    PRIMARY KEY (grading_id, question_id),
    FOREIGN KEY (grading_id) REFERENCES Grading(grading_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES Question(question_id) ON DELETE CASCADE
);



CREATE TABLE GradeBook (
    grade_book_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    grading_policy ENUM('TOTAL_POINTS', 'WEIGHTED', 'PASS_FAIL', 'COMPETENCY') NOT NULL,
    grading_scale TEXT DEFAULT NULL, -- JSON-based custom grading scale
    total_course_points DOUBLE DEFAULT 0, -- Total points of all course activities
    current_points_achieved DOUBLE DEFAULT 0, -- Points achieved so far
    total_weight DOUBLE DEFAULT NULL, -- Total weight of grade categories (for weighted grading)
    completion_percentage DOUBLE DEFAULT 0, -- Percentage of course completed
    enable_normalization BOOLEAN NOT NULL DEFAULT FALSE,
    custom_grading_scale TEXT DEFAULT NULL,
    metadata TEXT DEFAULT NULL, -- Additional metadata
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE
);


CREATE TABLE GradeBookCategory (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade_book_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL, -- Category name (e.g., Assignments, Quizzes)
    weight DOUBLE DEFAULT NULL, -- Weight of the category for weighted grading
    total_weight DOUBLE DEFAULT NULL,
    total_points DOUBLE DEFAULT 0, -- Total points for this category
    current_points_achieved DOUBLE DEFAULT 0, -- Points achieved in this category
    metadata TEXT DEFAULT NULL, -- Additional metadata
    enable_late_drop BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (grade_book_id) REFERENCES GradeBook(grade_book_id) ON DELETE CASCADE
);


CREATE TABLE GradeBookItem (
    item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL, -- Name of the gradeable activity (e.g., Quiz 1, Assignment 1)
    linked_entity_type ENUM('ASSIGNMENT', 'ASSESSMENT', 'PROJECT', 'OTHER') NOT NULL, -- Type of the linked activity
    linked_entity_id BIGINT NOT NULL, -- ID of the linked activity
    due_date TIMESTAMP DEFAULT NULL, -- Due date for the activity
    max_points DOUBLE NOT NULL, -- Maximum points for this activity
    current_points_achieved DOUBLE DEFAULT 0, -- Points achieved for this activity
    grading_id BIGINT DEFAULT NULL, -- Link to Grading table
    group_id BIGINT,
    grade_verification_required BOOLEAN NOT NULL DEFAULT FALSE,
    is_group_grading BOOLEAN NOT NULL DEFAULT FALSE,
    metadata TEXT DEFAULT NULL, -- Additional metadata
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES GradeBookCategory(category_id) ON DELETE CASCADE,
    FOREIGN KEY (grading_id) REFERENCES Grading(grading_id) ON DELETE CASCADE
    FOREIGN KEY (group_id) REFERENCES StudentGroup(group_id) ON DELETE CASCADE
);


CREATE TABLE GradeBookRecord (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    score DOUBLE DEFAULT NULL, -- Score achieved by the student
    feedback TEXT DEFAULT NULL, -- Feedback for the student
    is_finalized BOOLEAN NOT NULL DEFAULT FALSE, -- Whether the grade is finalized
    metadata TEXT DEFAULT NULL, -- Additional metadata
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES GradeBookItem(item_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE
);


CREATE TABLE GradeBookHistory (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id BIGINT NOT NULL,
    previous_score DOUBLE DEFAULT NULL, -- Previous score before the change
    new_score DOUBLE DEFAULT NULL, -- New score after the change
    changed_by BIGINT NOT NULL, -- User who made the change
    change_reason TEXT DEFAULT NULL, -- Reason for the grade change
    metadata TEXT DEFAULT NULL, -- Additional metadata
    date_changed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES GradeBookRecord(record_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES User(user_id) ON DELETE CASCADE
);



-- CREATE MESSAGE TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- FINAL
CREATE TABLE Message (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT DEFAULT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata TEXT DEFAULT NULL,
    FOREIGN KEY (thread_id) REFERENCES MessageThread(thread_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES User(user_id) ON DELETE CASCADE
);

-- CREATE MESSAGE THREAD TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
CREATE TABLE MessageThread (
    thread_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    is_group BOOLEAN NOT NULL DEFAULT FALSE,
    associated_entity_type ENUM('COURSE', 'ASSIGNMENT', 'OTHER') DEFAULT NULL,
    associated_entity_id BIGINT DEFAULT NULL,
    last_message_at TIMESTAMP DEFAULT NULL,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    metadata TEXT DEFAULT NULL
);


CREATE TABLE ThreadParticipants (
    thread_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (thread_id, user_id),
    FOREIGN KEY (thread_id) REFERENCES MessageThread(thread_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
);

-- CREATE MESSAGE ATTACHMENT TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
CREATE TABLE MessageAttachment (
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    url VARCHAR(255) NOT NULL,
    metadata TEXT DEFAULT NULL,
    date_uploaded TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES Message(message_id) ON DELETE CASCADE
);

-- CREATE MESSAGE REACTION TABLE  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
CREATE TABLE MessageReaction (
    reaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    emoji VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata TEXT DEFAULT NULL,
    FOREIGN KEY (message_id) REFERENCES Message(message_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
);


-- SYSTEM RELATED ENTITIES ++++++++++++++++++++++++++++++++++++++++++++++

CREATE TABLE GlobalSettings (
    setting_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    `group` VARCHAR(50) DEFAULT NULL,
    `key` VARCHAR(100) NOT NULL UNIQUE,
    value TEXT NOT NULL,
    description TEXT DEFAULT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);


CREATE TABLE SystemResourceLibrary (
    resource_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT DEFAULT NULL,
    resource_type ENUM('EBOOK', 'VIDEO', 'DOCUMENT', 'IMAGE') NOT NULL,
    url VARCHAR(255) NOT NULL,
    tags TEXT DEFAULT NULL,
    visibility ENUM('PUBLIC', 'PRIVATE', 'TENANT_ONLY') NOT NULL,
    owner_id BIGINT NOT NULL,
    metadata TEXT DEFAULT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES User(user_id) ON DELETE CASCADE
);


CREATE TABLE SystemFeedback (
    feedback_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    context_entity_type ENUM('COURSE', 'ASSIGNMENT', 'SYSTEM', 'OTHER') NOT NULL,
    context_entity_id BIGINT NOT NULL,
    submitted_by BIGINT NOT NULL,
    feedback_type ENUM('COMMENT', 'REVIEW', 'SUGGESTION') NOT NULL,
    content TEXT NOT NULL,
    rating INT DEFAULT NULL,
    visibility ENUM('PUBLIC', 'PRIVATE', 'INSTRUCTOR_ONLY') NOT NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    response TEXT DEFAULT NULL,
    status ENUM('PENDING', 'ADDRESSED', 'CLOSED') NOT NULL DEFAULT 'PENDING',
    metadata TEXT DEFAULT NULL,
    date_submitted TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_responded TIMESTAMP DEFAULT NULL,
    FOREIGN KEY (submitted_by) REFERENCES User(user_id) ON DELETE CASCADE
);


CREATE TABLE TenantSettings (
    setting_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    `key` VARCHAR(100) NOT NULL,
    value TEXT NOT NULL,
    description TEXT DEFAULT NULL,
    `group` VARCHAR(50) DEFAULT NULL,
    metadata TEXT DEFAULT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES Tenant(tenant_id) ON DELETE CASCADE
);



CREATE TABLE permissions (
    permission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    resource VARCHAR(100) NOT NULL,
    action ENUM('CREATE', 'READ', 'UPDATE', 'DELETE', 'MANAGE', 'EXECUTE') NOT NULL,
    scope VARCHAR(50) NOT NULL,
    role_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    conditions TEXT,
    expires_at DATETIME,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    parent_permission_id BIGINT,
    metadata TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    priority INT NOT NULL,
    group_id BIGINT,

    CONSTRAINT fk_role_id FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_parent_permission_id FOREIGN KEY (parent_permission_id) REFERENCES permissions(permission_id) ON DELETE SET NULL,
    CONSTRAINT fk_group_id FOREIGN KEY (group_id) REFERENCES permission_groups(group_id) ON DELETE SET NULL;
);



CREATE INDEX idx_resource_action ON permissions(resource, action);
CREATE INDEX idx_is_active ON permissions(is_active);
CREATE INDEX idx_priority ON permissions(priority);
CREATE INDEX idx_is_deleted ON permissions(is_deleted);


CREATE TABLE permission_groups (
    group_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id BIGINT, -- Nullable to support global groups
    metadata TEXT,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,

    CONSTRAINT fk_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id) ON DELETE SET NULL,
    CONSTRAINT fk_created_by FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_updated_by FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_is_active ON permission_groups(is_active);
CREATE INDEX idx_tenant_id ON permission_groups(tenant_id);




-- PROCTORING LOG TABLE
CREATE TABLE ProctoringLog (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    violations TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    success BOOLEAN NOT NULL,
    FOREIGN KEY (assessment_id) REFERENCES Assessment(assessment_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE
);

-- PROCTORING SESSION TABLE
CREATE TABLE ProctoringSession (
    proctoring_session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status ENUM('INITIATED', 'IN_PROGRESS', 'APPROVED', 'REJECTED') NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    metadata TEXT,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (assessment_id) REFERENCES Assessment(assessment_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE
);













--  FINAL ABOVE  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++




-- Add foreign key to reference the users table
ALTER TABLE students
ADD CONSTRAINT fk_students_users
FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE;



-- Add These Constraints for Notification
ALTER TABLE lms_notifications
ADD CONSTRAINT fk_notification_recipient
FOREIGN KEY (recipient_id)
REFERENCES users(user_id) ON DELETE CASCADE;

-- Add These Constraints for activity_logs
ALTER TABLE activity_logs
ADD CONSTRAINT fk_activity_log_user
FOREIGN KEY (user_id)
REFERENCES users(user_id) ON DELETE CASCADE;


-- Add These Constraints for courses
ALTER TABLE courses
ADD COLUMN teacher_id BIGINT,
ADD CONSTRAINT fk_teacher
FOREIGN KEY (teacher_id) REFERENCES staff(staff_id) ON DELETE SET NULL;



