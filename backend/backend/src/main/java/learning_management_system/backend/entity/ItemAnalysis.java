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
 * Table <b>item_analysis</b> (Assessment & Testing domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "item_analysis", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ItemAnalysis implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "analysis_id", updatable = false)
    private UUID analysisId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "total_attempts")
    private Integer totalAttempts;

    @Column(name = "correct_answers")
    private Integer correctAnswers;

    @Column(name = "incorrect_answers")
    private Integer incorrectAnswers;

    @Column(name = "skipped_count")
    private Integer skippedCount;

    @Column(name = "difficulty_index", precision = 5, scale = 2)
    private BigDecimal difficultyIndex;

    @Column(name = "discrimination_index", precision = 5, scale = 2)
    private BigDecimal discriminationIndex;

    @Column(name = "avg_time_seconds", precision = 8, scale = 2)
    private BigDecimal avgTimeSeconds;

    @Column(name = "point_biserial_correlation", precision = 5, scale = 3)
    private BigDecimal pointBiserialCorrelation;

    @Column(name = "is_flagged_for_review")
    private Boolean isFlaggedForReview;

    @Column(name = "flag_reason")
    private String flagReason;

    @Column(name = "date_analyzed", nullable = false)
    private OffsetDateTime dateAnalyzed;
}
