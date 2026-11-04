package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>feedback</b> (Assignments & Coursework domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "feedback", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Feedback implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "feedback_id", updatable = false)
    private UUID feedbackId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @Column(name = "evaluator_id", nullable = false)
    private UUID evaluatorId;

    @Column(name = "grade_earned", precision = 8, scale = 2)
    private BigDecimal gradeEarned;

    @Column(name = "grade_percentage", precision = 5, scale = 2)
    private BigDecimal gradePercentage;

    @Column(name = "grade_letter", length = 5)
    private String gradeLetter;

    @Column(name = "feedback_text")
    private String feedbackText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feedback_rubric_data")
    private JsonNode feedbackRubricData;

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "date_graded", nullable = false)
    private OffsetDateTime dateGraded;

    @Column(name = "grading_rubric_id")
    private UUID gradingRubricId;

    @Column(name = "grading_time_minutes")
    private Integer gradingTimeMinutes;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
