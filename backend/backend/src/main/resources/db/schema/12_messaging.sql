-- ================================================================================
-- 12. MESSAGING (5 tables)
-- ================================================================================
-- Tables: message_thread, thread_participant, message, message_reaction, message_read_status
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- MessageThread: Direct message conversations
CREATE TABLE message_thread (
    thread_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    thread_subject VARCHAR(255),
    thread_type VARCHAR(50) NOT NULL,
    creator_id UUID NOT NULL,
    is_archived BOOLEAN DEFAULT FALSE,
    is_muted BOOLEAN DEFAULT FALSE,
    last_message_id UUID,
    last_message_at TIMESTAMPTZ,
    total_messages INT DEFAULT 0,
    total_participants INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_message_thread_type CHECK (thread_type IN ('DIRECT', 'GROUP', 'SUPPORT', 'ANNOUNCEMENT', 'NOTIFICATION')),
    CONSTRAINT ck_message_thread_subject CHECK (thread_subject IS NULL OR LENGTH(thread_subject) >= 3)
);

-- ThreadParticipants: Users participating in a message thread
CREATE TABLE thread_participant (
    participant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    thread_id UUID NOT NULL,
    user_id UUID NOT NULL,
    participant_role VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMPTZ,
    last_read_message_id UUID,
    last_read_at TIMESTAMPTZ,
    unread_count INT DEFAULT 0,
    is_muted BOOLEAN DEFAULT FALSE,
    notification_enabled BOOLEAN DEFAULT TRUE,
    notification_frequency VARCHAR(50) DEFAULT 'IMMEDIATE',
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_thread_participant UNIQUE (thread_id, user_id),
    CONSTRAINT ck_thread_participant_role CHECK (participant_role IN ('CREATOR', 'PARTICIPANT', 'MODERATOR', 'ADMIN')),
    CONSTRAINT ck_thread_participant_frequency CHECK (notification_frequency IN ('IMMEDIATE', 'DAILY', 'WEEKLY', 'NEVER'))
);

-- Message: Individual messages in a thread
CREATE TABLE message (
    message_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    thread_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    parent_message_id UUID,
    message_subject VARCHAR(255),
    message_content TEXT NOT NULL,
    message_html JSONB,
    message_type VARCHAR(50) NOT NULL,
    priority VARCHAR(50) DEFAULT 'NORMAL',
    is_read_only BOOLEAN DEFAULT FALSE,
    is_pinned BOOLEAN DEFAULT FALSE,
    is_edited BOOLEAN DEFAULT FALSE,
    edited_at TIMESTAMPTZ,
    edited_by UUID,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    delete_reason TEXT,
    total_reactions INT DEFAULT 0,
    total_replies INT DEFAULT 0,
    ip_address INET,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_message_content CHECK (LENGTH(message_content) >= 1),
    CONSTRAINT ck_message_type CHECK (message_type IN ('TEXT', 'SYSTEM', 'NOTIFICATION', 'NOTIFICATION_DIGEST', 'AUTO_REPLY')),
    CONSTRAINT ck_message_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    CONSTRAINT ck_message_subject CHECK (message_subject IS NULL OR LENGTH(message_subject) >= 3)
);

-- MessageReaction: Reactions/emojis on messages
CREATE TABLE message_reaction (
    reaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    message_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reaction_type VARCHAR(50) NOT NULL,
    reaction_emoji VARCHAR(10),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_message_reaction UNIQUE (message_id, user_id),
    CONSTRAINT ck_message_reaction_type CHECK (reaction_type IN ('LIKE', 'LOVE', 'LAUGH', 'AMAZED', 'SAD', 'ANGRY', 'THUMBSUP', 'THUMBSDOWN', 'CUSTOM'))
);

-- MessageReadStatus: Track which messages have been read by participants
CREATE TABLE message_read_status (
    read_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    message_id UUID NOT NULL,
    user_id UUID NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    date_read TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_message_read_status UNIQUE (message_id, user_id)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_message_thread_tenant ON message_thread(tenant_id);
CREATE INDEX idx_message_thread_creator ON message_thread(creator_id);
CREATE INDEX idx_message_thread_type ON message_thread(thread_type);
CREATE INDEX idx_message_thread_is_archived ON message_thread(is_archived) WHERE is_archived = TRUE;
CREATE INDEX idx_message_thread_last_message ON message_thread(last_message_at DESC);
CREATE INDEX idx_thread_participant_thread ON thread_participant(thread_id);
CREATE INDEX idx_thread_participant_user ON thread_participant(user_id);
CREATE INDEX idx_thread_participant_is_active ON thread_participant(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_thread_participant_unread ON thread_participant(unread_count) WHERE unread_count > 0;
CREATE INDEX idx_thread_participant_last_read ON thread_participant(last_read_at DESC);
CREATE INDEX idx_message_thread ON message(thread_id);
CREATE INDEX idx_message_sender ON message(sender_id);
CREATE INDEX idx_message_parent ON message(parent_message_id);
CREATE INDEX idx_message_date_created ON message(date_created DESC);
CREATE INDEX idx_message_is_deleted ON message(is_deleted) WHERE is_deleted = TRUE;
CREATE INDEX idx_message_is_pinned ON message(is_pinned) WHERE is_pinned = TRUE;
CREATE INDEX idx_message_type ON message(message_type);
CREATE INDEX idx_message_priority ON message(priority);
CREATE INDEX idx_message_reaction_message ON message_reaction(message_id);
CREATE INDEX idx_message_reaction_user ON message_reaction(user_id);
CREATE INDEX idx_message_reaction_type ON message_reaction(reaction_type);
CREATE INDEX idx_message_read_status_message ON message_read_status(message_id);
CREATE INDEX idx_message_read_status_user ON message_read_status(user_id);
CREATE INDEX idx_message_read_status_is_read ON message_read_status(is_read) WHERE is_read = FALSE;
CREATE INDEX idx_thread_participant_tenant ON thread_participant(tenant_id);
CREATE INDEX idx_message_tenant ON message(tenant_id);
CREATE INDEX idx_message_reaction_tenant ON message_reaction(tenant_id);
CREATE INDEX idx_message_read_status_tenant ON message_read_status(tenant_id);
CREATE INDEX idx_message_deleted_by_fk ON message(deleted_by);
CREATE INDEX idx_message_edited_by_fk ON message(edited_by);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_message_thread_date_updated BEFORE UPDATE ON message_thread FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_message_date_updated BEFORE UPDATE ON message FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE message_thread ENABLE ROW LEVEL SECURITY;
ALTER TABLE message_thread FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_message_thread_tenant ON message_thread FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE thread_participant ENABLE ROW LEVEL SECURITY;
ALTER TABLE thread_participant FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_thread_participant_tenant ON thread_participant FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE message ENABLE ROW LEVEL SECURITY;
ALTER TABLE message FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_message_tenant ON message FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE message_reaction ENABLE ROW LEVEL SECURITY;
ALTER TABLE message_reaction FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_message_reaction_tenant ON message_reaction FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE message_read_status ENABLE ROW LEVEL SECURITY;
ALTER TABLE message_read_status FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_message_read_status_tenant ON message_read_status FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
