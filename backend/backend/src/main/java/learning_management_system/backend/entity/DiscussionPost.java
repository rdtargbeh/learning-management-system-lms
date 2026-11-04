package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.DiscussionPostModerationStatus;
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
 * Table <b>discussion_post</b> (Discussion & Collaboration domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "discussion_post", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class DiscussionPost implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_id", updatable = false)
    private UUID postId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "thread_id", nullable = false)
    private UUID threadId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "parent_post_id")
    private UUID parentPostId;

    @Column(name = "post_content", nullable = false)
    private String postContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "post_html")
    private JsonNode postHtml;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous;

    @Column(name = "is_moderated")
    private Boolean isModerated;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", length = 50)
    private DiscussionPostModerationStatus moderationStatus;

    @Column(name = "is_edited")
    private Boolean isEdited;

    @Column(name = "edited_by")
    private UUID editedBy;

    @Column(name = "edited_at")
    private OffsetDateTime editedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "delete_reason")
    private String deleteReason;

    @Column(name = "reputation_points")
    private Integer reputationPoints;

    @Column(name = "total_reactions")
    private Integer totalReactions;

    @Column(name = "total_replies")
    private Integer totalReplies;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
