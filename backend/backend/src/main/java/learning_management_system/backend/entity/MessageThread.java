package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ThreadType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>message_thread</b> (Messaging domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "message_thread", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class MessageThread implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "thread_id", updatable = false)
    private UUID threadId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "thread_subject", length = 255)
    private String threadSubject;

    @Enumerated(EnumType.STRING)
    @Column(name = "thread_type", nullable = false, length = 50)
    private ThreadType threadType;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @Column(name = "is_muted")
    private Boolean isMuted;

    @Column(name = "last_message_id")
    private UUID lastMessageId;

    @Column(name = "last_message_at")
    private OffsetDateTime lastMessageAt;

    @Column(name = "total_messages")
    private Integer totalMessages;

    @Column(name = "total_participants")
    private Integer totalParticipants;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
