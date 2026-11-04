-- ================================================================================
-- 11. DISCUSSION & COLLABORATION (11 tables)
-- ================================================================================
-- Tables: discussion, discussion_thread, discussion_post, discussion_mention, discussion_reaction, discussion_subscription, discussion_analytics, collaboration, collaboration_member, discussion_checkpoint, discussion_post_read
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- Discussion: Discussion forum/board definitions
CREATE TABLE discussion (
    discussion_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID NOT NULL,
    module_id UUID,
    group_set_id UUID,
    discussion_name VARCHAR(255) NOT NULL,
    discussion_description TEXT,
    discussion_type VARCHAR(50) NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    allow_anonymous BOOLEAN DEFAULT FALSE,
    allow_rating BOOLEAN NOT NULL DEFAULT FALSE,
    sort_by_rating BOOLEAN NOT NULL DEFAULT FALSE,
    require_initial_post BOOLEAN DEFAULT FALSE,
    require_replies BOOLEAN DEFAULT FALSE,
    min_replies_required INT,
    display_order INT DEFAULT 0,
    graded BOOLEAN DEFAULT FALSE,
    has_checkpoints BOOLEAN NOT NULL DEFAULT FALSE,
    points_possible NUMERIC(8,2),
    date_available DATE,
    date_due DATE,
    date_close DATE,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_discussion_name CHECK (LENGTH(discussion_name) >= 3),
    CONSTRAINT ck_discussion_min_replies CHECK (min_replies_required IS NULL OR min_replies_required >= 0),
    CONSTRAINT ck_discussion_points CHECK (points_possible IS NULL OR points_possible > 0),
    CONSTRAINT ck_discussion_dates CHECK (date_close IS NULL OR date_close >= date_available),
    CONSTRAINT ck_discussion_type CHECK (discussion_type IN ('FORUM','WHITEBOARD','BLOG','Q_AND_A','CHAT','DEBATE','JOURNAL','WIKI'))
);

-- DiscussionThread: Topic threads within a discussion
CREATE TABLE discussion_thread (
    thread_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    discussion_id UUID NOT NULL,
    thread_title VARCHAR(255) NOT NULL,
    thread_description TEXT,
    created_by UUID NOT NULL,
    is_pinned BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    is_moderated BOOLEAN DEFAULT FALSE,
    moderation_status VARCHAR(50) DEFAULT 'APPROVED',
    total_posts INT DEFAULT 1,
    total_views INT DEFAULT 0,
    last_post_at TIMESTAMPTZ,
    thread_status VARCHAR(50) DEFAULT 'ACTIVE',
    display_order INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_discussion_thread_title CHECK (LENGTH(thread_title) >= 3),
    CONSTRAINT ck_discussion_thread_moderation CHECK (moderation_status IN ('PENDING', 'APPROVED', 'REJECTED', 'FLAGGED')),
    CONSTRAINT ck_discussion_thread_status CHECK (thread_status IN ('ACTIVE', 'ARCHIVED', 'CLOSED', 'DELETED'))
);

-- DiscussionPost: Individual posts/messages in a thread
CREATE TABLE discussion_post (
    post_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    thread_id UUID NOT NULL,
    created_by UUID NOT NULL,
    parent_post_id UUID,
    post_content TEXT NOT NULL,
    post_html JSONB,
    is_anonymous BOOLEAN DEFAULT FALSE,
    is_moderated BOOLEAN DEFAULT FALSE,
    moderation_status VARCHAR(50) DEFAULT 'APPROVED',
    is_edited BOOLEAN DEFAULT FALSE,
    edited_by UUID,
    edited_at TIMESTAMPTZ,
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_by UUID,
    deleted_at TIMESTAMPTZ,
    delete_reason TEXT,
    reputation_points INT DEFAULT 0,
    total_reactions INT DEFAULT 0,
    total_replies INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_discussion_post_content CHECK (LENGTH(post_content) >= 1),
    CONSTRAINT ck_discussion_post_moderation CHECK (moderation_status IN ('PENDING', 'APPROVED', 'REJECTED', 'FLAGGED')),
    CONSTRAINT ck_discussion_post_reputation CHECK (reputation_points >= -100 AND reputation_points <= 100)
);

-- DiscussionMention: @mentions of users in posts
CREATE TABLE discussion_mention (
    mention_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    post_id UUID NOT NULL,
    mentioned_user_id UUID NOT NULL,
    mention_position INT,
    is_notified BOOLEAN DEFAULT FALSE,
    date_notified TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_discussion_mention UNIQUE (post_id, mentioned_user_id)
);

-- DiscussionReaction: Reactions/emojis on posts
CREATE TABLE discussion_reaction (
    reaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    post_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reaction_type VARCHAR(50) NOT NULL,
    reaction_emoji VARCHAR(10),
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_discussion_reaction UNIQUE (post_id, user_id),
    CONSTRAINT ck_discussion_reaction_type CHECK (reaction_type IN ('LIKE', 'LOVE', 'LAUGH', 'AMAZED', 'SAD', 'ANGRY', 'HELPFUL', 'CUSTOM'))
);

-- DiscussionSubscription: User subscriptions to discussions/threads
CREATE TABLE discussion_subscription (
    subscription_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    user_id UUID NOT NULL,
    discussion_id UUID,
    thread_id UUID,
    subscription_level VARCHAR(50) NOT NULL,
    notification_enabled BOOLEAN DEFAULT TRUE,
    notification_frequency VARCHAR(50) DEFAULT 'IMMEDIATE',
    date_subscribed TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_unsubscribed TIMESTAMPTZ,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT ck_discussion_subscription_level CHECK (subscription_level IN ('DISCUSSION', 'THREAD', 'MENTION')),
    CONSTRAINT ck_discussion_subscription_frequency CHECK (notification_frequency IN ('IMMEDIATE', 'DAILY', 'WEEKLY', 'DIGEST', 'NEVER')),
    CONSTRAINT ck_discussion_subscription_either CHECK ((discussion_id IS NOT NULL AND thread_id IS NULL) OR (discussion_id IS NULL AND thread_id IS NOT NULL))
);

-- DiscussionAnalytics: Aggregate analytics for discussions
CREATE TABLE discussion_analytics (
    analytics_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    discussion_id UUID NOT NULL,
    total_threads INT DEFAULT 0,
    total_posts INT DEFAULT 0,
    total_unique_participants INT DEFAULT 0,
    total_views INT DEFAULT 0,
    avg_posts_per_thread NUMERIC(8,2) DEFAULT 0,
    avg_reply_time_minutes NUMERIC(8,2),
    most_active_user_id UUID,
    most_active_time_slot VARCHAR(50),
    sentiment_positive_percent NUMERIC(5,2),
    sentiment_neutral_percent NUMERIC(5,2),
    sentiment_negative_percent NUMERIC(5,2),
    moderation_flags INT DEFAULT 0,
    deleted_posts INT DEFAULT 0,
    engagement_score NUMERIC(5,2),
    last_analyzed TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_discussion_analytics UNIQUE (discussion_id),
    CONSTRAINT ck_discussion_analytics_sentiment CHECK ( sentiment_positive_percent IS NULL OR (sentiment_positive_percent >= 0 AND sentiment_positive_percent <= 100 AND sentiment_neutral_percent >= 0 AND sentiment_neutral_percent <= 100 AND sentiment_negative_percent >= 0 AND sentiment_negative_percent <= 100 AND (sentiment_positive_percent + sentiment_neutral_percent + sentiment_negative_percent) <= 100) )
);

CREATE TABLE collaboration (
    collaboration_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID,
    group_id UUID,
    provider VARCHAR(30) NOT NULL,
    collaboration_title VARCHAR(255) NOT NULL,
    description TEXT,
    document_url VARCHAR(1000) NOT NULL,
    created_by UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_collaboration_context CHECK (num_nonnulls(course_id, group_id) >= 1),
    CONSTRAINT ck_collaboration_provider CHECK (provider IN ('GOOGLE_DOCS','MICROSOFT_365','LTI','OTHER'))
);

CREATE TABLE collaboration_member (
    collaboration_member_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    collaboration_id UUID NOT NULL,
    user_id UUID,
    group_id UUID,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_collaboration_member_one CHECK (num_nonnulls(user_id, group_id) = 1)
);

CREATE TABLE discussion_checkpoint (
    checkpoint_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    discussion_id UUID NOT NULL,
    checkpoint_type VARCHAR(20) NOT NULL,
    points_possible NUMERIC(8,2) NOT NULL,
    required_count INT NOT NULL DEFAULT 1,
    date_due TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_discussion_checkpoint UNIQUE (discussion_id, checkpoint_type),
    CONSTRAINT ck_discussion_checkpoint_type CHECK (checkpoint_type IN ('REPLY_TO_TOPIC','REPLY_TO_ENTRY')),
    CONSTRAINT ck_discussion_checkpoint_points CHECK (points_possible >= 0 AND required_count > 0)
);

CREATE TABLE discussion_post_read (
    post_read_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    post_id UUID NOT NULL,
    user_id UUID NOT NULL,
    is_forced_read BOOLEAN NOT NULL DEFAULT FALSE,
    date_read TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_discussion_post_read UNIQUE (post_id, user_id)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_discussion_tenant ON discussion(tenant_id);
CREATE INDEX idx_discussion_course ON discussion(course_id);
CREATE INDEX idx_discussion_module ON discussion(module_id);
CREATE INDEX idx_discussion_type ON discussion(discussion_type);
CREATE INDEX idx_discussion_is_published ON discussion(is_published);
CREATE INDEX idx_discussion_is_locked ON discussion(is_locked) WHERE is_locked = TRUE;
CREATE INDEX idx_discussion_graded ON discussion(graded) WHERE graded = TRUE;
CREATE INDEX idx_discussion_display_order ON discussion(course_id, module_id, display_order);
CREATE INDEX idx_discussion_thread_discussion ON discussion_thread(discussion_id);
CREATE INDEX idx_discussion_thread_created_by ON discussion_thread(created_by);
CREATE INDEX idx_discussion_thread_is_pinned ON discussion_thread(is_pinned) WHERE is_pinned = TRUE;
CREATE INDEX idx_discussion_thread_is_locked ON discussion_thread(is_locked) WHERE is_locked = TRUE;
CREATE INDEX idx_discussion_thread_status ON discussion_thread(thread_status);
CREATE INDEX idx_discussion_thread_last_post ON discussion_thread(last_post_at DESC);
CREATE INDEX idx_discussion_post_thread ON discussion_post(thread_id);
CREATE INDEX idx_discussion_post_created_by ON discussion_post(created_by);
CREATE INDEX idx_discussion_post_parent ON discussion_post(parent_post_id);
CREATE INDEX idx_discussion_post_moderation ON discussion_post(moderation_status) WHERE moderation_status != 'APPROVED';
CREATE INDEX idx_discussion_post_is_deleted ON discussion_post(is_deleted) WHERE is_deleted = TRUE;
CREATE INDEX idx_discussion_post_date_created ON discussion_post(date_created DESC);
CREATE INDEX idx_discussion_post_edited ON discussion_post(is_edited) WHERE is_edited = TRUE;
CREATE INDEX idx_discussion_mention_post ON discussion_mention(post_id);
CREATE INDEX idx_discussion_mention_user ON discussion_mention(mentioned_user_id);
CREATE INDEX idx_discussion_mention_notified ON discussion_mention(is_notified) WHERE is_notified = FALSE;
CREATE INDEX idx_discussion_reaction_post ON discussion_reaction(post_id);
CREATE INDEX idx_discussion_reaction_user ON discussion_reaction(user_id);
CREATE INDEX idx_discussion_reaction_type ON discussion_reaction(reaction_type);
CREATE INDEX idx_discussion_subscription_user ON discussion_subscription(user_id);
CREATE INDEX idx_discussion_subscription_discussion ON discussion_subscription(discussion_id);
CREATE INDEX idx_discussion_subscription_thread ON discussion_subscription(thread_id);
CREATE INDEX idx_discussion_subscription_is_active ON discussion_subscription(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_discussion_subscription_notification ON discussion_subscription(notification_enabled) WHERE notification_enabled = TRUE;
CREATE INDEX idx_discussion_analytics_last_analyzed ON discussion_analytics(last_analyzed DESC);
CREATE INDEX idx_discussion_analytics_engagement ON discussion_analytics(engagement_score DESC);
CREATE INDEX idx_discussion_thread_tenant ON discussion_thread(tenant_id);
CREATE INDEX idx_discussion_post_tenant ON discussion_post(tenant_id);
CREATE INDEX idx_discussion_mention_tenant ON discussion_mention(tenant_id);
CREATE INDEX idx_discussion_reaction_tenant ON discussion_reaction(tenant_id);
CREATE INDEX idx_discussion_subscription_tenant ON discussion_subscription(tenant_id);
CREATE INDEX idx_discussion_analytics_tenant ON discussion_analytics(tenant_id);
CREATE INDEX idx_discussion_analytics_most_active_user_id_fk ON discussion_analytics(most_active_user_id);
CREATE INDEX idx_discussion_post_deleted_by_fk ON discussion_post(deleted_by);
CREATE INDEX idx_discussion_post_edited_by_fk ON discussion_post(edited_by);
CREATE INDEX idx_collaboration_course_id_fk ON collaboration(course_id);
CREATE INDEX idx_collaboration_created_by_fk ON collaboration(created_by);
CREATE INDEX idx_collaboration_group_id_fk ON collaboration(group_id);
CREATE INDEX idx_collaboration_member_collaboration_id_fk ON collaboration_member(collaboration_id);
CREATE INDEX idx_collaboration_member_group_id_fk ON collaboration_member(group_id);
CREATE INDEX idx_collaboration_member_tenant_id_fk ON collaboration_member(tenant_id);
CREATE INDEX idx_collaboration_member_user_id_fk ON collaboration_member(user_id);
CREATE INDEX idx_collaboration_tenant_id_fk ON collaboration(tenant_id);
CREATE INDEX idx_discussion_checkpoint_tenant_id_fk ON discussion_checkpoint(tenant_id);
CREATE INDEX idx_discussion_group_set_id_fk ON discussion(group_set_id);
CREATE INDEX idx_discussion_post_read_tenant_id_fk ON discussion_post_read(tenant_id);
CREATE INDEX idx_discussion_post_read_user_id_fk ON discussion_post_read(user_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_discussion_date_updated BEFORE UPDATE ON discussion FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_discussion_thread_date_updated BEFORE UPDATE ON discussion_thread FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_discussion_post_date_updated BEFORE UPDATE ON discussion_post FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_collaboration_date_updated BEFORE UPDATE ON collaboration FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_discussion_checkpoint_date_updated BEFORE UPDATE ON discussion_checkpoint FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE discussion ENABLE ROW LEVEL SECURITY;
ALTER TABLE discussion FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_discussion_tenant ON discussion FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE discussion_thread ENABLE ROW LEVEL SECURITY;
ALTER TABLE discussion_thread FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_discussion_thread_tenant ON discussion_thread FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE discussion_post ENABLE ROW LEVEL SECURITY;
ALTER TABLE discussion_post FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_discussion_post_tenant ON discussion_post FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE discussion_mention ENABLE ROW LEVEL SECURITY;
ALTER TABLE discussion_mention FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_discussion_mention_tenant ON discussion_mention FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE discussion_reaction ENABLE ROW LEVEL SECURITY;
ALTER TABLE discussion_reaction FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_discussion_reaction_tenant ON discussion_reaction FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE discussion_subscription ENABLE ROW LEVEL SECURITY;
ALTER TABLE discussion_subscription FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_discussion_subscription_tenant ON discussion_subscription FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE discussion_analytics ENABLE ROW LEVEL SECURITY;
ALTER TABLE discussion_analytics FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_discussion_analytics_tenant ON discussion_analytics FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE collaboration ENABLE ROW LEVEL SECURITY;
ALTER TABLE collaboration FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_collaboration_tenant ON collaboration FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE collaboration_member ENABLE ROW LEVEL SECURITY;
ALTER TABLE collaboration_member FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_collaboration_member_tenant ON collaboration_member FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE discussion_checkpoint ENABLE ROW LEVEL SECURITY;
ALTER TABLE discussion_checkpoint FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_discussion_checkpoint_tenant ON discussion_checkpoint FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE discussion_post_read ENABLE ROW LEVEL SECURITY;
ALTER TABLE discussion_post_read FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_discussion_post_read_tenant ON discussion_post_read FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
