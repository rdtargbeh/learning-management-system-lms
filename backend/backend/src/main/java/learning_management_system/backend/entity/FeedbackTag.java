package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.TagCategory;
import learning_management_system.backend.enums.TagSeverity;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>feedback_tag</b> (Assignments & Coursework domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "feedback_tag", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class FeedbackTag implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tag_id", updatable = false)
    private UUID tagId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "feedback_id", nullable = false)
    private UUID feedbackId;

    @Column(name = "tag_name", nullable = false, length = 100)
    private String tagName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_category", length = 50)
    private TagCategory tagCategory;

    @Column(name = "tag_position_start")
    private Integer tagPositionStart;

    @Column(name = "tag_position_end")
    private Integer tagPositionEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_severity", length = 50)
    private TagSeverity tagSeverity;

    @Column(name = "suggestion")
    private String suggestion;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
