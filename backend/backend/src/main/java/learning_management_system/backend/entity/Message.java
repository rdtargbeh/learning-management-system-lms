package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.MessagePriority;
import learning_management_system.backend.enums.MessageType;
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
 * Table <b>message</b> (Messaging domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "message", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Message implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "message_id", updatable = false)
    private UUID messageId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "thread_id", nullable = false)
    private UUID threadId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "parent_message_id")
    private UUID parentMessageId;

    @Column(name = "message_subject", length = 255)
    private String messageSubject;

    @Column(name = "message_content", nullable = false)
    private String messageContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "message_html")
    private JsonNode messageHtml;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 50)
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 50)
    private MessagePriority priority;

    @Column(name = "is_read_only")
    private Boolean isReadOnly;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @Column(name = "is_edited")
    private Boolean isEdited;

    @Column(name = "edited_at")
    private OffsetDateTime editedAt;

    @Column(name = "edited_by")
    private UUID editedBy;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Column(name = "delete_reason")
    private String deleteReason;

    @Column(name = "total_reactions")
    private Integer totalReactions;

    @Column(name = "total_replies")
    private Integer totalReplies;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
