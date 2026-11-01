package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ModuleCompletionRequirementType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>module_completion_requirement</b> (Learning Content Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "module_completion_requirement", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ModuleCompletionRequirement implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "requirement_id", updatable = false)
    private UUID requirementId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "module_id", nullable = false)
    private UUID moduleId;

    @Column(name = "content_id")
    private UUID contentId;

    @Column(name = "assignment_id")
    private UUID assignmentId;

    @Column(name = "assessment_id")
    private UUID assessmentId;

    @Column(name = "discussion_id")
    private UUID discussionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type", nullable = false, length = 30)
    private ModuleCompletionRequirementType requirementType;

    @Column(name = "min_score", precision = 8, scale = 2)
    private BigDecimal minScore;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
