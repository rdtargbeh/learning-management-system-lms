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
 * Table <b>gradebook_period_grade</b> (Grading & Evaluation domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "gradebook_period_grade", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class GradebookPeriodGrade implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "period_grade_id", updatable = false)
    private UUID periodGradeId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "gradebook_id", nullable = false)
    private UUID gradebookId;

    @Column(name = "grading_period_id", nullable = false)
    private UUID gradingPeriodId;

    @Column(name = "current_score", precision = 6, scale = 2)
    private BigDecimal currentScore;

    @Column(name = "final_score", precision = 6, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "override_score", precision = 6, scale = 2)
    private BigDecimal overrideScore;

    @Column(name = "letter_grade", length = 10)
    private String letterGrade;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
