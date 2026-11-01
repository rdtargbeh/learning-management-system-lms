package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.DiscussionThreadModerationStatus;
import learning_management_system.backend.enums.ThreadStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>discussion_thread</b> (Discussion & Collaboration domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "discussion_thread", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class DiscussionThread implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "thread_id", updatable = false)
    private UUID threadId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "discussion_id", nullable = false)
    private UUID discussionId;

    @Column(name = "thread_title", nullable = false, length = 255)
    private String threadTitle;

    @Column(name = "thread_description")
    private String threadDescription;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "is_pinned")
    private Boolean isPinned;

    @Column(name = "is_locked")
    private Boolean isLocked;

    @Column(name = "is_moderated")
    private Boolean isModerated;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", length = 50)
    private DiscussionThreadModerationStatus moderationStatus;

    @Column(name = "total_posts")
    private Integer totalPosts;

    @Column(name = "total_views")
    private Integer totalViews;

    @Column(name = "last_post_at")
    private OffsetDateTime lastPostAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "thread_status", length = 50)
    private ThreadStatus threadStatus;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
