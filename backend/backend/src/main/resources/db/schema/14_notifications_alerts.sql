-- ================================================================================
-- 14. NOTIFICATIONS & ALERTS (5 tables)
-- ================================================================================
-- Tables: notification_template, notification, notification_preference, notification_queue, communication_channel
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- NotificationTemplate: Message templates for notifications
CREATE TABLE notification_template (
    template_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    template_name VARCHAR(100) NOT NULL,
    template_category VARCHAR(50),
    subject_template TEXT NOT NULL,
    body_template TEXT NOT NULL,
    icon_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Notification: Individual notifications to users
CREATE TABLE notification (
    notification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    template_id UUID,
    title VARCHAR(255),
    message TEXT,
    notification_type VARCHAR(50),
    entity_type VARCHAR(100),
    entity_id UUID,
    action_url VARCHAR(500),
    priority VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    date_read TIMESTAMPTZ,
    is_archived BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- NotificationPreference: User notification preferences
CREATE TABLE notification_preference (
    preference_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL UNIQUE,
    email_notifications_enabled BOOLEAN DEFAULT TRUE,
    email_frequency VARCHAR(50),
    in_app_notifications_enabled BOOLEAN DEFAULT TRUE,
    sms_notifications_enabled BOOLEAN DEFAULT FALSE,
    push_notifications_enabled BOOLEAN DEFAULT TRUE,
    quiet_hours_enabled BOOLEAN DEFAULT FALSE,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR(50),
    notification_settings JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- NotificationQueue: For async delivery
CREATE TABLE notification_queue (
    queue_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    notification_id UUID NOT NULL,
    delivery_channel VARCHAR(50),
    status VARCHAR(50),
    retry_count INT DEFAULT 0,
    next_retry TIMESTAMPTZ,
    error_message TEXT,
    date_queued TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_sent TIMESTAMPTZ
);

CREATE TABLE communication_channel (
    channel_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID NOT NULL,
    channel_type VARCHAR(20) NOT NULL,
    address VARCHAR(500) NOT NULL,
    device_platform VARCHAR(20),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    date_verified TIMESTAMPTZ,
    position INT NOT NULL DEFAULT 0,
    bounce_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_communication_channel UNIQUE (user_id, channel_type, address),
    CONSTRAINT ck_communication_channel_type CHECK (channel_type IN ('EMAIL','SMS','PUSH','SLACK','TEAMS','WEBHOOK')),
    CONSTRAINT ck_communication_channel_platform CHECK (device_platform IS NULL OR device_platform IN ('IOS','ANDROID','WEB'))
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_notification_template_tenant ON notification_template(tenant_id);
CREATE INDEX idx_notification_template_category ON notification_template(template_category);
CREATE INDEX idx_notification_user ON notification(user_id);
CREATE INDEX idx_notification_is_read ON notification(is_read);
CREATE INDEX idx_notification_date_created ON notification(date_created DESC);
CREATE INDEX idx_notification_entity ON notification(entity_type, entity_id);
CREATE INDEX idx_notification_preference_tenant ON notification_preference(tenant_id);
CREATE INDEX idx_notification_queue_tenant ON notification_queue(tenant_id);
CREATE INDEX idx_notification_queue_status ON notification_queue(status);
CREATE INDEX idx_notification_queue_next_retry ON notification_queue(next_retry);
CREATE INDEX idx_notification_queue_notification_id_fk ON notification_queue(notification_id);
CREATE INDEX idx_notification_template_id_fk ON notification(template_id);
CREATE INDEX idx_notification_tenant_id_fk ON notification(tenant_id);
CREATE INDEX idx_communication_channel_tenant_id_fk ON communication_channel(tenant_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_notification_preference_date_updated BEFORE UPDATE ON notification_preference FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_communication_channel_date_updated BEFORE UPDATE ON communication_channel FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_notification_preference_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON notification_preference FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

CREATE TRIGGER trg_communication_channel_inherit_tenant BEFORE INSERT OR UPDATE OF user_id ON communication_channel FOR EACH ROW EXECUTE FUNCTION inherit_user_tenant();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE notification_template ENABLE ROW LEVEL SECURITY;
ALTER TABLE notification_template FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_notification_template_tenant ON notification_template FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE notification ENABLE ROW LEVEL SECURITY;
ALTER TABLE notification FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_notification_tenant ON notification FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE notification_preference ENABLE ROW LEVEL SECURITY;
ALTER TABLE notification_preference FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_notification_preference_private ON notification_preference FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
ALTER TABLE notification_queue ENABLE ROW LEVEL SECURITY;
ALTER TABLE notification_queue FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_notification_queue_tenant ON notification_queue FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE communication_channel ENABLE ROW LEVEL SECURITY;
ALTER TABLE communication_channel FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_communication_channel_private ON communication_channel FOR ALL USING ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id()) WITH CHECK ((tenant_id IS NOT NULL AND tenant_id = app_current_tenant_id()) OR app_is_platform_admin() OR user_id = app_current_user_id());
