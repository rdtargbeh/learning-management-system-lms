package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.CriteriaType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>badge_criteria</b> (Gamification & Achievements domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "badge_criteria", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class BadgeCriteria implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "criteria_id", updatable = false)
    private UUID criteriaId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "badge_id", nullable = false)
    private UUID badgeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "criteria_type", nullable = false, length = 50)
    private CriteriaType criteriaType;

    @Column(name = "criteria_name", length = 255)
    private String criteriaName;

    @Column(name = "criteria_description")
    private String criteriaDescription;

    @Column(name = "criteria_operator", length = 10)
    private String criteriaOperator;

    @Column(name = "criteria_value")
    private Integer criteriaValue;

    @Column(name = "criteria_threshold", precision = 8, scale = 2)
    private BigDecimal criteriaThreshold;

    @Column(name = "time_period", length = 50)
    private String timePeriod;

    @Column(name = "is_cumulative")
    private Boolean isCumulative;

    @Column(name = "requires_all_conditions")
    private Boolean requiresAllConditions;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
