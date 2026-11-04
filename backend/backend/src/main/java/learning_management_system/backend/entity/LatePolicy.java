package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LateDeductionInterval;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>late_policy</b> (Grading & Evaluation domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "late_policy", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class LatePolicy implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "late_policy_id", updatable = false)
    private UUID latePolicyId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "late_deduction_enabled", nullable = false)
    private Boolean lateDeductionEnabled;

    @Column(name = "late_deduction_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal lateDeductionPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "late_deduction_interval", nullable = false, length = 10)
    private LateDeductionInterval lateDeductionInterval;

    @Column(name = "late_minimum_percent", precision = 5, scale = 2)
    private BigDecimal lateMinimumPercent;

    @Column(name = "missing_deduction_enabled", nullable = false)
    private Boolean missingDeductionEnabled;

    @Column(name = "missing_deduction_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal missingDeductionPercent;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
