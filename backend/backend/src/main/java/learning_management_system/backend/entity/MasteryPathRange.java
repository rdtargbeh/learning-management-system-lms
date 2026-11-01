package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Table <b>mastery_path_range</b> (Learning Content Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "mastery_path_range", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class MasteryPathRange implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "range_id", updatable = false)
    private UUID rangeId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "lower_bound_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal lowerBoundPercent;

    @Column(name = "upper_bound_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal upperBoundPercent;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}
