package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AnnouncementType;
import learning_management_system.backend.enums.AnnouncementVisibility;
import learning_management_system.backend.enums.TargetAudience;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>announcement</b> (Announcements & Notifications domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "announcement", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Announcement implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "announcement_id", updatable = false)
    private UUID announcementId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "announcement_title", nullable = false, length = 255)
    private String announcementTitle;

    @Column(name = "announcement_content", nullable = false)
    private String announcementContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "announcement_html")
    private JsonNode announcementHtml;

    @Enumerated(EnumType.STRING)
    @Column(name = "announcement_type", nullable = false, length = 50)
    private AnnouncementType announcementType;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @Column(name = "is_locked")
    private Boolean isLocked;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "start_display_date")
    private LocalDate startDisplayDate;

    @Column(name = "end_display_date")
    private LocalDate endDisplayDate;

    @Column(name = "delay_posting")
    private Boolean delayPosting;

    @Column(name = "delayed_until")
    private OffsetDateTime delayedUntil;

    @Column(name = "require_read_confirmation")
    private Boolean requireReadConfirmation;

    @Column(name = "read_confirmation_deadline")
    private OffsetDateTime readConfirmationDeadline;

    @Column(name = "allow_comments")
    private Boolean allowComments;

    @Column(name = "moderate_comments")
    private Boolean moderateComments;

    @Column(name = "send_notifications")
    private Boolean sendNotifications;

    @Column(name = "notification_sent_at")
    private OffsetDateTime notificationSentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", length = 50)
    private TargetAudience targetAudience;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 50)
    private AnnouncementVisibility visibility;

    @Column(name = "total_comments")
    private Integer totalComments;

    @Column(name = "total_views")
    private Integer totalViews;

    @Column(name = "total_reads")
    private Integer totalReads;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
