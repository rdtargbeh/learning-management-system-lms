package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AssignmentPeerReviewStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>assignment_peer_review</b> (Assignments & Coursework domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "assignment_peer_review", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AssignmentPeerReview implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "peer_review_id", updatable = false)
    private UUID peerReviewId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 50)
    private AssignmentPeerReviewStatus reviewStatus;

    @Column(name = "review_text")
    private String reviewText;

    @Column(name = "review_rating")
    private Integer reviewRating;

    @Column(name = "plagiarism_score", precision = 5, scale = 2)
    private BigDecimal plagiarismScore;

    @Column(name = "plagiarism_detected")
    private Boolean plagiarismDetected;

    @Column(name = "date_assigned", nullable = false)
    private OffsetDateTime dateAssigned;

    @Column(name = "date_submitted")
    private OffsetDateTime dateSubmitted;

    @Column(name = "anonymous_review")
    private Boolean anonymousReview;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
