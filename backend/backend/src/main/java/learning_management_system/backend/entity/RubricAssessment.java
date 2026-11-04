package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AssessmentPurpose;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>rubric_assessment</b> (Grading & Evaluation domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "rubric_assessment", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class RubricAssessment implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "assessment_id", updatable = false)
    private UUID assessmentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "grade_entry_id", nullable = false)
    private UUID gradeEntryId;

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "assessor_id", nullable = false)
    private UUID assessorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_purpose", nullable = false, length = 20)
    private AssessmentPurpose assessmentPurpose;

    @Column(name = "total_score", precision = 8, scale = 2)
    private BigDecimal totalScore;

    @Column(name = "total_percentage", precision = 5, scale = 2)
    private BigDecimal totalPercentage;

    @Column(name = "assessment_feedback")
    private String assessmentFeedback;

    @Column(name = "overall_comments")
    private String overallComments;

    @Column(name = "assessment_date", nullable = false)
    private OffsetDateTime assessmentDate;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
