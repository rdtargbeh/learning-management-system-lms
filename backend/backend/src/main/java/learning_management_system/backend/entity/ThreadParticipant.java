package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ThreadParticipantNotificationFrequency;
import learning_management_system.backend.enums.ThreadParticipantRole;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>thread_participant</b> (Messaging domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "thread_participant", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ThreadParticipant implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "participant_id", updatable = false)
    private UUID participantId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "thread_id", nullable = false)
    private UUID threadId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_role", nullable = false, length = 50)
    private ThreadParticipantRole participantRole;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "left_at")
    private OffsetDateTime leftAt;

    @Column(name = "last_read_message_id")
    private UUID lastReadMessageId;

    @Column(name = "last_read_at")
    private OffsetDateTime lastReadAt;

    @Column(name = "unread_count")
    private Integer unreadCount;

    @Column(name = "is_muted")
    private Boolean isMuted;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_frequency", length = 50)
    private ThreadParticipantNotificationFrequency notificationFrequency;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
