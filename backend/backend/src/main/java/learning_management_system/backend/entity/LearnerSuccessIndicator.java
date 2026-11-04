package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AttendanceTrend;
import learning_management_system.backend.enums.InterventionPriority;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>learner_success_indicator</b> (Learning Analytics & Reporting domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "learner_success_indicator", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class LearnerSuccessIndicator implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "indicator_id", updatable = false)
    private UUID indicatorId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;

    @Column(name = "current_grade", precision = 5, scale = 2)
    private BigDecimal currentGrade;

    @Column(name = "predicted_grade", precision = 5, scale = 2)
    private BigDecimal predictedGrade;

    @Column(name = "predicted_pass_probability", precision = 5, scale = 2)
    private BigDecimal predictedPassProbability;

    @Column(name = "predicted_completion_probability", precision = 5, scale = 2)
    private BigDecimal predictedCompletionProbability;

    @Column(name = "risk_score", precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(name = "success_score", precision = 5, scale = 2)
    private BigDecimal successScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_trend", length = 50)
    private AttendanceTrend attendanceTrend;

    @Column(name = "assignment_trend", length = 50)
    private String assignmentTrend;

    @Column(name = "assessment_trend", length = 50)
    private String assessmentTrend;

    @Column(name = "engagement_trend", length = 50)
    private String engagementTrend;

    @Column(name = "content_access_trend", length = 50)
    private String contentAccessTrend;

    @Column(name = "collaboration_trend", length = 50)
    private String collaborationTrend;

    @Column(name = "early_warning_flag")
    private Boolean earlyWarningFlag;

    @Column(name = "early_warning_reason", length = 255)
    private String earlyWarningReason;

    @Column(name = "intervention_recommended")
    private Boolean interventionRecommended;

    @Column(name = "recommended_intervention")
    private String recommendedIntervention;

    @Column(name = "time_to_intervention_hours")
    private Integer timeToInterventionHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "intervention_priority", length = 50)
    private InterventionPriority interventionPriority;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
