package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AssessmentStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>assessment</b> (Assessment & Testing domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "assessment", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Assessment implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "assessment_id", updatable = false)
    private UUID assessmentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "type_id", nullable = false)
    private UUID typeId;

    @Column(name = "assessment_title", nullable = false, length = 255)
    private String assessmentTitle;

    @Column(name = "assessment_description")
    private String assessmentDescription;

    @Column(name = "instructions")
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_status", nullable = false, length = 50)
    private AssessmentStatus assessmentStatus;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "shuffle_questions")
    private Boolean shuffleQuestions;

    @Column(name = "shuffle_options")
    private Boolean shuffleOptions;

    @Column(name = "show_one_question_at_time")
    private Boolean showOneQuestionAtTime;

    @Column(name = "allow_review")
    private Boolean allowReview;

    @Column(name = "show_correct_answers")
    private Boolean showCorrectAnswers;

    @Column(name = "show_score_immediately")
    private Boolean showScoreImmediately;

    @Column(name = "allow_multiple_attempts")
    private Boolean allowMultipleAttempts;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "scoring_type", length = 50)
    private String scoringType;

    @Column(name = "pass_score", precision = 5, scale = 2)
    private BigDecimal passScore;

    @Column(name = "total_points", precision = 8, scale = 2)
    private BigDecimal totalPoints;

    @Column(name = "display_points")
    private Boolean displayPoints;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "date_available")
    private LocalDate dateAvailable;

    @Column(name = "date_close")
    private LocalDate dateClose;

    @Column(name = "create_for_group")
    private Boolean createForGroup;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
