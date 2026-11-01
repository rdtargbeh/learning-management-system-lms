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
import org.hibernate.annotations.Generated;

/**
 * Table <b>discussion_mention</b> (Discussion & Collaboration domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "discussion_mention", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class DiscussionMention implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "mention_id", updatable = false)
    private UUID mentionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "mentioned_user_id", nullable = false)
    private UUID mentionedUserId;

    @Column(name = "mention_position")
    private Integer mentionPosition;

    @Column(name = "is_notified")
    private Boolean isNotified;

    @Column(name = "date_notified")
    private OffsetDateTime dateNotified;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
