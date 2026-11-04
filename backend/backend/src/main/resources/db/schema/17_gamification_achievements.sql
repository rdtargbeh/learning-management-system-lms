-- ================================================================================
-- 17. GAMIFICATION & ACHIEVEMENTS (6 tables)
-- ================================================================================
-- Tables: badge, badge_criteria, student_badge, badge_progress, achievement, leaderboard
-- Foreign keys for these tables are in 31_foreign_keys.sql (they reference other domains).
SET search_path TO lms_core, public;

-- ---------------------------------------------------------------- TABLES
-- Badge: Badge definitions
CREATE TABLE badge (
    badge_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    badge_name VARCHAR(255) NOT NULL,
    badge_description TEXT,
    badge_icon_url VARCHAR(500),
    badge_category VARCHAR(50) NOT NULL,
    badge_rarity VARCHAR(50) NOT NULL,
    points_value INT,
    xp_value INT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_hidden BOOLEAN DEFAULT FALSE,
    is_special BOOLEAN DEFAULT FALSE,
    open_badge_class JSONB,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_badge_name CHECK (LENGTH(badge_name) >= 3),
    CONSTRAINT ck_badge_category CHECK (badge_category IN ('LEARNING', 'ENGAGEMENT', 'COLLABORATION', 'ACHIEVEMENT', 'MILESTONE', 'SPECIAL', 'CHALLENGE')),
    CONSTRAINT ck_badge_rarity CHECK (badge_rarity IN ('COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY')),
    CONSTRAINT ck_badge_points CHECK (points_value IS NULL OR points_value > 0),
    CONSTRAINT ck_badge_xp CHECK (xp_value IS NULL OR xp_value > 0)
);

-- BadgeCriteria: Conditions for earning badges
CREATE TABLE badge_criteria (
    criteria_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    badge_id UUID NOT NULL,
    criteria_type VARCHAR(50) NOT NULL,
    criteria_name VARCHAR(255),
    criteria_description TEXT,
    criteria_operator VARCHAR(10),
    criteria_value INT,
    criteria_threshold NUMERIC(8,2),
    time_period VARCHAR(50),
    is_cumulative BOOLEAN DEFAULT FALSE,
    requires_all_conditions BOOLEAN DEFAULT TRUE,
    order_index INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_badge_criteria_type CHECK (criteria_type IN ('ASSESSMENT_SCORE', 'ASSIGNMENT_COMPLETION', 'DISCUSSION_POSTS', 'ATTENDANCE', 'COURSE_COMPLETION', 'STREAK', 'COLLABORATION', 'PEER_REVIEW', 'CONTENT_MASTERY', 'SPEED_COMPLETION', 'CUSTOM')),
    CONSTRAINT ck_badge_criteria_operator CHECK (criteria_operator IN ('=', '>', '>=', '<', '<=', '!=', 'BETWEEN')),
    CONSTRAINT ck_badge_criteria_value CHECK (criteria_value IS NULL OR criteria_value > 0)
);

-- StudentBadge: Badges earned by students
CREATE TABLE student_badge (
    student_badge_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    badge_id UUID NOT NULL,
    user_id UUID NOT NULL,
    earned_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    earned_in_course_id UUID,
    earned_in_section_id UUID,
    earned_through_activity_id UUID,
    earned_through_activity_type VARCHAR(100),
    is_pinned BOOLEAN DEFAULT FALSE,
    pinned_date TIMESTAMPTZ,
    is_visible BOOLEAN DEFAULT TRUE,
    assertion_url VARCHAR(1000),
    date_expires TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_student_badge UNIQUE (badge_id, user_id, earned_date)
);

-- BadgeProgress: Track progress toward earning badges
CREATE TABLE badge_progress (
    progress_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    badge_id UUID NOT NULL,
    user_id UUID NOT NULL,
    course_id UUID,
    section_id UUID,
    progress_percentage NUMERIC(5,2),
    criteria_met JSONB,
    current_value INT DEFAULT 0,
    target_value INT,
    last_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_completed BOOLEAN DEFAULT FALSE,
    completion_date TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_badge_progress UNIQUE (badge_id, user_id, course_id),
    CONSTRAINT ck_badge_progress_percentage CHECK (progress_percentage >= 0 AND progress_percentage <= 100)
);

-- Achievement: Major accomplishments and milestones
CREATE TABLE achievement (
    achievement_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    achievement_name VARCHAR(255) NOT NULL,
    achievement_description TEXT,
    achievement_icon_url VARCHAR(500),
    achievement_type VARCHAR(50) NOT NULL,
    achievement_level VARCHAR(50),
    points_reward INT,
    xp_reward INT,
    unlock_criteria TEXT,
    unlock_badge_ids JSONB,
    is_hidden BOOLEAN DEFAULT FALSE,
    is_unlocked_by_default BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_achievement_name CHECK (LENGTH(achievement_name) >= 3),
    CONSTRAINT ck_achievement_type CHECK (achievement_type IN ('COURSE_COMPLETION', 'GRADE_MILESTONE', 'PARTICIPATION', 'COMMUNITY', 'CHALLENGE', 'SOCIAL', 'LEADERSHIP', 'ACADEMIC_EXCELLENCE')),
    CONSTRAINT ck_achievement_level CHECK (achievement_level IS NULL OR achievement_level IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM')),
    CONSTRAINT ck_achievement_points CHECK (points_reward IS NULL OR points_reward > 0),
    CONSTRAINT ck_achievement_xp CHECK (xp_reward IS NULL OR xp_reward > 0)
);

-- Leaderboard: Leaderboards for gamification
CREATE TABLE leaderboard (
    leaderboard_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL DEFAULT app_current_tenant_id(),
    course_id UUID,
    section_id UUID,
    leaderboard_name VARCHAR(255) NOT NULL,
    leaderboard_description TEXT,
    leaderboard_type VARCHAR(50) NOT NULL,
    ranking_metric VARCHAR(50) NOT NULL,
    time_period VARCHAR(50),
    display_limit INT DEFAULT 50,
    is_public BOOLEAN DEFAULT TRUE,
    is_anonymous BOOLEAN DEFAULT FALSE,
    include_top_performers BOOLEAN DEFAULT TRUE,
    include_peer_group BOOLEAN DEFAULT FALSE,
    peer_group_size INT,
    start_date DATE,
    end_date DATE,
    allow_ties BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    last_updated TIMESTAMPTZ,
    date_created TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_leaderboard_name CHECK (LENGTH(leaderboard_name) >= 3),
    CONSTRAINT ck_leaderboard_type CHECK (leaderboard_type IN ('POINTS', 'XP', 'BADGES', 'GRADES', 'STREAK', 'PARTICIPATION', 'CUSTOM')),
    CONSTRAINT ck_leaderboard_metric CHECK (ranking_metric IN ('POINTS', 'XP', 'BADGE_COUNT', 'AVERAGE_GRADE', 'PARTICIPATION_SCORE', 'STREAK_DAYS', 'ACHIEVEMENT_COUNT')),
    CONSTRAINT ck_leaderboard_time_period CHECK (time_period IN ('ALL_TIME', 'SEMESTER', 'QUARTER', 'MONTHLY', 'WEEKLY', 'DAILY')),
    CONSTRAINT ck_leaderboard_display_limit CHECK (display_limit > 0),
    CONSTRAINT ck_leaderboard_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

-- ---------------------------------------------------------------- INDEXES
CREATE INDEX idx_badge_tenant ON badge(tenant_id);
CREATE INDEX idx_badge_category ON badge(badge_category);
CREATE INDEX idx_badge_rarity ON badge(badge_rarity);
CREATE INDEX idx_badge_is_active ON badge(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_badge_is_special ON badge(is_special) WHERE is_special = TRUE;
CREATE INDEX idx_badge_criteria_badge ON badge_criteria(badge_id);
CREATE INDEX idx_badge_criteria_type ON badge_criteria(criteria_type);
CREATE INDEX idx_student_badge_badge ON student_badge(badge_id);
CREATE INDEX idx_student_badge_user ON student_badge(user_id);
CREATE INDEX idx_student_badge_course ON student_badge(earned_in_course_id);
CREATE INDEX idx_student_badge_earned_date ON student_badge(earned_date DESC);
CREATE INDEX idx_student_badge_is_pinned ON student_badge(is_pinned) WHERE is_pinned = TRUE;
CREATE INDEX idx_badge_progress_badge ON badge_progress(badge_id);
CREATE INDEX idx_badge_progress_user ON badge_progress(user_id);
CREATE INDEX idx_badge_progress_course ON badge_progress(course_id);
CREATE INDEX idx_badge_progress_percentage ON badge_progress(progress_percentage DESC);
CREATE INDEX idx_badge_progress_is_completed ON badge_progress(is_completed) WHERE is_completed = FALSE;
CREATE INDEX idx_achievement_tenant ON achievement(tenant_id);
CREATE INDEX idx_achievement_type ON achievement(achievement_type);
CREATE INDEX idx_achievement_level ON achievement(achievement_level);
CREATE INDEX idx_achievement_is_hidden ON achievement(is_hidden) WHERE is_hidden = TRUE;
CREATE INDEX idx_leaderboard_tenant ON leaderboard(tenant_id);
CREATE INDEX idx_leaderboard_course ON leaderboard(course_id);
CREATE INDEX idx_leaderboard_section ON leaderboard(section_id);
CREATE INDEX idx_leaderboard_type ON leaderboard(leaderboard_type);
CREATE INDEX idx_leaderboard_metric ON leaderboard(ranking_metric);
CREATE INDEX idx_leaderboard_is_active ON leaderboard(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_leaderboard_last_updated ON leaderboard(last_updated DESC);
CREATE INDEX idx_badge_criteria_tenant ON badge_criteria(tenant_id);
CREATE INDEX idx_student_badge_tenant ON student_badge(tenant_id);
CREATE INDEX idx_badge_progress_tenant ON badge_progress(tenant_id);
CREATE INDEX idx_badge_progress_section_id_fk ON badge_progress(section_id);
CREATE INDEX idx_student_badge_earned_in_section_id_fk ON student_badge(earned_in_section_id);

-- ---------------------------------------------------------------- TRIGGERS
CREATE TRIGGER trg_badge_date_updated BEFORE UPDATE ON badge FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_achievement_date_updated BEFORE UPDATE ON achievement FOR EACH ROW EXECUTE FUNCTION set_date_updated();

CREATE TRIGGER trg_leaderboard_date_updated BEFORE UPDATE ON leaderboard FOR EACH ROW EXECUTE FUNCTION set_date_updated();

-- ---------------------------------------------------------------- ROW LEVEL SECURITY
ALTER TABLE badge ENABLE ROW LEVEL SECURITY;
ALTER TABLE badge FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_badge_tenant ON badge FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE badge_criteria ENABLE ROW LEVEL SECURITY;
ALTER TABLE badge_criteria FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_badge_criteria_tenant ON badge_criteria FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE student_badge ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_badge FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_student_badge_tenant ON student_badge FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE badge_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE badge_progress FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_badge_progress_tenant ON badge_progress FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE achievement ENABLE ROW LEVEL SECURITY;
ALTER TABLE achievement FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_achievement_tenant ON achievement FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
ALTER TABLE leaderboard ENABLE ROW LEVEL SECURITY;
ALTER TABLE leaderboard FORCE ROW LEVEL SECURITY;
CREATE POLICY rls_leaderboard_tenant ON leaderboard FOR ALL USING (tenant_id = app_current_tenant_id() OR app_is_platform_admin()) WITH CHECK (tenant_id = app_current_tenant_id() OR app_is_platform_admin());
