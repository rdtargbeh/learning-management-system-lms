package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.DiscussionSubscriptionNotificationFrequency;
import learning_management_system.backend.enums.SubscriptionLevel;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Table <b>discussion_subscription</b> (Discussion & Collaboration domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "discussion_subscription", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class DiscussionSubscription implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "subscription_id", updatable = false)
    private UUID subscriptionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "discussion_id")
    private UUID discussionId;

    @Column(name = "thread_id")
    private UUID threadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_level", nullable = false, length = 50)
    private SubscriptionLevel subscriptionLevel;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_frequency", length = 50)
    private DiscussionSubscriptionNotificationFrequency notificationFrequency;

    @Column(name = "date_subscribed", nullable = false)
    private OffsetDateTime dateSubscribed;

    @Column(name = "date_unsubscribed")
    private OffsetDateTime dateUnsubscribed;

    @Column(name = "is_active")
    private Boolean isActive;
}
