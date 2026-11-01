package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>rubric_level</b> (Grading & Evaluation domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "rubric_level", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class RubricLevel implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "level_id", updatable = false)
    private UUID levelId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "criteria_id", nullable = false)
    private UUID criteriaId;

    @Column(name = "level_name", nullable = false, length = 100)
    private String levelName;

    @Column(name = "level_description")
    private String levelDescription;

    @Column(name = "points", nullable = false, precision = 8, scale = 2)
    private BigDecimal points;

    @Column(name = "level_order", nullable = false)
    private Integer levelOrder;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
