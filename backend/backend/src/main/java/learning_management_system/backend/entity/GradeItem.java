package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.GradeItemGradingType;
import learning_management_system.backend.enums.GradeItemType;
import learning_management_system.backend.enums.PostPolicy;
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
 * Table <b>grade_item</b> (Grading & Evaluation domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "grade_item", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class GradeItem implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_id", updatable = false)
    private UUID itemId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "assignment_id")
    private UUID assignmentId;

    @Column(name = "assessment_id")
    private UUID assessmentId;

    @Column(name = "discussion_id")
    private UUID discussionId;

    @Column(name = "discussion_checkpoint_id")
    private UUID discussionCheckpointId;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Column(name = "item_description")
    private String itemDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private GradeItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "grading_type", nullable = false, length = 20)
    private GradeItemGradingType gradingType;

    @Column(name = "points_possible", precision = 8, scale = 2)
    private BigDecimal pointsPossible;

    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculation_formula")
    private JsonNode calculationFormula;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "external_id", length = 255)
    private String externalId;

    @Column(name = "external_source", length = 50)
    private String externalSource;

    @Column(name = "include_in_final_grade")
    private Boolean includeInFinalGrade;

    @Column(name = "is_extra_credit")
    private Boolean isExtraCredit;

    @Column(name = "is_published")
    private Boolean isPublished;

    @Column(name = "is_muted", nullable = false)
    private Boolean isMuted;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_policy", nullable = false, length = 10)
    private PostPolicy postPolicy;

    @Column(name = "date_scheduled_post")
    private OffsetDateTime dateScheduledPost;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
