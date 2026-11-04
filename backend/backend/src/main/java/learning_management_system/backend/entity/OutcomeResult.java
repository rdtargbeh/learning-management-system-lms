package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.OutcomeResultSourceType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>outcome_result</b> (Learning Standards & Compliance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "outcome_result", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class OutcomeResult implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "outcome_result_id", updatable = false)
    private UUID outcomeResultId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "outcome_id", nullable = false)
    private UUID outcomeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private OutcomeResultSourceType sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "score", precision = 8, scale = 2)
    private BigDecimal score;

    @Column(name = "possible", precision = 8, scale = 2)
    private BigDecimal possible;

    @Column(name = "mastery_level", length = 30)
    private String masteryLevel;

    @Column(name = "is_mastered")
    private Boolean isMastered;

    @Column(name = "date_assessed", nullable = false)
    private OffsetDateTime dateAssessed;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
