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
 * Table <b>provisional_grade</b> (Grading & Evaluation domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "provisional_grade", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ProvisionalGrade implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "provisional_grade_id", updatable = false)
    private UUID provisionalGradeId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @Column(name = "grader_user_id", nullable = false)
    private UUID graderUserId;

    @Column(name = "score", precision = 8, scale = 2)
    private BigDecimal score;

    @Column(name = "grade_letter", length = 10)
    private String gradeLetter;

    @Column(name = "feedback_text")
    private String feedbackText;

    @Column(name = "rubric_assessment_id")
    private UUID rubricAssessmentId;

    @Column(name = "is_selected", nullable = false)
    private Boolean isSelected;

    @Column(name = "selected_by")
    private UUID selectedBy;

    @Column(name = "date_selected")
    private OffsetDateTime dateSelected;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
