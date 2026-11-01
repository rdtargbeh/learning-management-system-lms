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
 * Table <b>gradebook</b> (Grading & Evaluation domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "gradebook", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Gradebook implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "gradebook_id", updatable = false)
    private UUID gradebookId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "final_grade", precision = 8, scale = 2)
    private BigDecimal finalGrade;

    @Column(name = "final_grade_percentage", precision = 5, scale = 2)
    private BigDecimal finalGradePercentage;

    @Column(name = "final_grade_letter", length = 5)
    private String finalGradeLetter;

    @Column(name = "final_grade_points", precision = 8, scale = 2)
    private BigDecimal finalGradePoints;

    @Column(name = "current_grade", precision = 8, scale = 2)
    private BigDecimal currentGrade;

    @Column(name = "current_grade_percentage", precision = 5, scale = 2)
    private BigDecimal currentGradePercentage;

    @Column(name = "override_percentage", precision = 6, scale = 2)
    private BigDecimal overridePercentage;

    @Column(name = "override_letter", length = 10)
    private String overrideLetter;

    @Column(name = "overridden_by")
    private UUID overriddenBy;

    @Column(name = "date_overridden")
    private OffsetDateTime dateOverridden;

    @Column(name = "grade_scale_id")
    private UUID gradeScaleId;

    @Column(name = "is_locked")
    private Boolean isLocked;

    @Column(name = "date_locked")
    private OffsetDateTime dateLocked;

    @Column(name = "locked_by")
    private UUID lockedBy;

    @Column(name = "last_grade_update")
    private OffsetDateTime lastGradeUpdate;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
