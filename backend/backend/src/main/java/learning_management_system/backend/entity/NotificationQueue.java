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
 * Table <b>notification_queue</b> (Notifications & Alerts domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "notification_queue", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class NotificationQueue implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "queue_id", updatable = false)
    private UUID queueId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    @Column(name = "delivery_channel", length = 50)
    private String deliveryChannel;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "next_retry")
    private OffsetDateTime nextRetry;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "date_queued", nullable = false)
    private OffsetDateTime dateQueued;

    @Column(name = "date_sent")
    private OffsetDateTime dateSent;
}
