package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Table <b>discussion_analytics</b> (Discussion & Collaboration domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "discussion_analytics", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class DiscussionAnalytics implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "analytics_id", updatable = false)
    private UUID analyticsId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "discussion_id", nullable = false)
    private UUID discussionId;

    @Column(name = "total_threads")
    private Integer totalThreads;

    @Column(name = "total_posts")
    private Integer totalPosts;

    @Column(name = "total_unique_participants")
    private Integer totalUniqueParticipants;

    @Column(name = "total_views")
    private Integer totalViews;

    @Column(name = "avg_posts_per_thread", precision = 8, scale = 2)
    private BigDecimal avgPostsPerThread;

    @Column(name = "avg_reply_time_minutes", precision = 8, scale = 2)
    private BigDecimal avgReplyTimeMinutes;

    @Column(name = "most_active_user_id")
    private UUID mostActiveUserId;

    @Column(name = "most_active_time_slot", length = 50)
    private String mostActiveTimeSlot;

    @Column(name = "sentiment_positive_percent", precision = 5, scale = 2)
    private BigDecimal sentimentPositivePercent;

    @Column(name = "sentiment_neutral_percent", precision = 5, scale = 2)
    private BigDecimal sentimentNeutralPercent;

    @Column(name = "sentiment_negative_percent", precision = 5, scale = 2)
    private BigDecimal sentimentNegativePercent;

    @Column(name = "moderation_flags")
    private Integer moderationFlags;

    @Column(name = "deleted_posts")
    private Integer deletedPosts;

    @Column(name = "engagement_score", precision = 5, scale = 2)
    private BigDecimal engagementScore;

    @Column(name = "last_analyzed", nullable = false)
    private OffsetDateTime lastAnalyzed;
}
