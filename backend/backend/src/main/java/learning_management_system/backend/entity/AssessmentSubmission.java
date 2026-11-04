package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AssessmentSubmissionStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>assessment_submission</b> (Assessment & Testing domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "assessment_submission", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AssessmentSubmission implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "submission_id", updatable = false)
    private UUID submissionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_status", nullable = false, length = 50)
    private AssessmentSubmissionStatus submissionStatus;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    @Column(name = "graded_at")
    private OffsetDateTime gradedAt;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "final_percentage", precision = 5, scale = 2)
    private BigDecimal finalPercentage;

    @Column(name = "appeal_requested")
    private Boolean appealRequested;

    @Column(name = "appeal_reason")
    private String appealReason;

    @Column(name = "appeal_requested_at")
    private OffsetDateTime appealRequestedAt;

    @Column(name = "appeal_reviewed_at")
    private OffsetDateTime appealReviewedAt;

    @Column(name = "appeal_decision")
    private String appealDecision;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
