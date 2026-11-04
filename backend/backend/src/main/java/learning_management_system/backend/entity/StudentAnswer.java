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

/**
 * Table <b>student_answer</b> (Assessment & Testing domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "student_answer", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class StudentAnswer implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "answer_id", updatable = false)
    private UUID answerId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "option_id")
    private UUID optionId;

    @Column(name = "text_answer")
    private String textAnswer;

    @Column(name = "numeric_answer", precision = 20, scale = 4)
    private BigDecimal numericAnswer;

    @Column(name = "is_marked_for_review")
    private Boolean isMarkedForReview;

    @Column(name = "answer_time_seconds")
    private Integer answerTimeSeconds;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "points_earned", precision = 8, scale = 2)
    private BigDecimal pointsEarned;

    @Column(name = "graded_by")
    private UUID gradedBy;

    @Column(name = "grading_notes")
    private String gradingNotes;

    @Column(name = "date_answered", nullable = false)
    private OffsetDateTime dateAnswered;

    @Column(name = "date_graded")
    private OffsetDateTime dateGraded;
}
