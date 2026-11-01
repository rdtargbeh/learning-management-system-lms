package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.EngagementLevel;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>engagement_metric</b> (Learning Analytics & Reporting domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "engagement_metric", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class EngagementMetric implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "metric_id", updatable = false)
    private UUID metricId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "dataset_id", nullable = false)
    private UUID datasetId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "content_views")
    private Integer contentViews;

    @Column(name = "content_downloads")
    private Integer contentDownloads;

    @Column(name = "discussion_posts")
    private Integer discussionPosts;

    @Column(name = "discussion_replies")
    private Integer discussionReplies;

    @Column(name = "messages_sent")
    private Integer messagesSent;

    @Column(name = "login_count")
    private Integer loginCount;

    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes;

    @Column(name = "assignment_submissions")
    private Integer assignmentSubmissions;

    @Column(name = "assessment_attempts")
    private Integer assessmentAttempts;

    @Column(name = "video_watch_time_minutes")
    private Integer videoWatchTimeMinutes;

    @Column(name = "interaction_count")
    private Integer interactionCount;

    @Column(name = "engagement_score", precision = 5, scale = 2)
    private BigDecimal engagementScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "engagement_level", length = 50)
    private EngagementLevel engagementLevel;

    @Column(name = "last_activity")
    private OffsetDateTime lastActivity;

    @Column(name = "is_at_risk")
    private Boolean isAtRisk;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
