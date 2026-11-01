package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.DiscussionType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>discussion</b> (Discussion & Collaboration domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "discussion", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Discussion implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "discussion_id", updatable = false)
    private UUID discussionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "group_set_id")
    private UUID groupSetId;

    @Column(name = "discussion_name", nullable = false, length = 255)
    private String discussionName;

    @Column(name = "discussion_description")
    private String discussionDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "discussion_type", nullable = false, length = 50)
    private DiscussionType discussionType;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Column(name = "allow_anonymous")
    private Boolean allowAnonymous;

    @Column(name = "allow_rating", nullable = false)
    private Boolean allowRating;

    @Column(name = "sort_by_rating", nullable = false)
    private Boolean sortByRating;

    @Column(name = "require_initial_post")
    private Boolean requireInitialPost;

    @Column(name = "require_replies")
    private Boolean requireReplies;

    @Column(name = "min_replies_required")
    private Integer minRepliesRequired;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "graded")
    private Boolean graded;

    @Column(name = "has_checkpoints", nullable = false)
    private Boolean hasCheckpoints;

    @Column(name = "points_possible", precision = 8, scale = 2)
    private BigDecimal pointsPossible;

    @Column(name = "date_available")
    private LocalDate dateAvailable;

    @Column(name = "date_due")
    private LocalDate dateDue;

    @Column(name = "date_close")
    private LocalDate dateClose;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
