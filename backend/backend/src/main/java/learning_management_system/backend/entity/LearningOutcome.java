package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.CalculationMethod;
import learning_management_system.backend.enums.LearningOutcomeBloomLevel;
import learning_management_system.backend.enums.OutcomeType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>learning_outcome</b> (Academic Structure domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "learning_outcome", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class LearningOutcome implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "outcome_id", updatable = false)
    private UUID outcomeId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "org_unit_id")
    private UUID orgUnitId;

    @Column(name = "outcome_number", nullable = false)
    private Integer outcomeNumber;

    @Column(name = "outcome_description", nullable = false)
    private String outcomeDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "bloom_level", length = 50)
    private LearningOutcomeBloomLevel bloomLevel;

    @Column(name = "mastery_points", precision = 8, scale = 2)
    private BigDecimal masteryPoints;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false, length = 20)
    private CalculationMethod calculationMethod;

    @Column(name = "calculation_int")
    private Integer calculationInt;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome_type", length = 50)
    private OutcomeType outcomeType;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "assessment_method", length = 100)
    private String assessmentMethod;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
