package learning_management_system.backend.entity;

import jakarta.persistence.*;
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
 * Table <b>announcement_view</b> (Announcements & Notifications domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "announcement_view", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AnnouncementView implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "view_id", updatable = false)
    private UUID viewId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "announcement_id", nullable = false)
    private UUID announcementId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "date_first_viewed", nullable = false)
    private OffsetDateTime dateFirstViewed;

    @Column(name = "date_last_viewed", nullable = false)
    private OffsetDateTime dateLastViewed;

    @Column(name = "date_read")
    private OffsetDateTime dateRead;

    @Column(name = "read_confirmation_submitted")
    private Boolean readConfirmationSubmitted;

    @Column(name = "confirmation_submitted_at")
    private OffsetDateTime confirmationSubmittedAt;
}
