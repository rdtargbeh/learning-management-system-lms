package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.HealthStatus;
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
 * Table <b>course_health_check</b> (Learning Analytics & Reporting domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "course_health_check", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class CourseHealthCheck implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "health_id", updatable = false)
    private UUID healthId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @Column(name = "total_students")
    private Integer totalStudents;

    @Column(name = "active_students")
    private Integer activeStudents;

    @Column(name = "inactive_students")
    private Integer inactiveStudents;

    @Column(name = "avg_engagement_score", precision = 5, scale = 2)
    private BigDecimal avgEngagementScore;

    @Column(name = "avg_attendance_percentage", precision = 5, scale = 2)
    private BigDecimal avgAttendancePercentage;

    @Column(name = "avg_grade", precision = 5, scale = 2)
    private BigDecimal avgGrade;

    @Column(name = "pass_rate", precision = 5, scale = 2)
    private BigDecimal passRate;

    @Column(name = "completion_rate", precision = 5, scale = 2)
    private BigDecimal completionRate;

    @Column(name = "dropout_risk_count")
    private Integer dropoutRiskCount;

    @Column(name = "content_completion_rate", precision = 5, scale = 2)
    private BigDecimal contentCompletionRate;

    @Column(name = "discussion_participation_rate", precision = 5, scale = 2)
    private BigDecimal discussionParticipationRate;

    @Column(name = "assignment_submission_rate", precision = 5, scale = 2)
    private BigDecimal assignmentSubmissionRate;

    @Column(name = "assessment_completion_rate", precision = 5, scale = 2)
    private BigDecimal assessmentCompletionRate;

    @Column(name = "instructor_response_time_hours", precision = 8, scale = 2)
    private BigDecimal instructorResponseTimeHours;

    @Column(name = "course_difficulty_rating", precision = 3, scale = 1)
    private BigDecimal courseDifficultyRating;

    @Column(name = "student_satisfaction_score", precision = 3, scale = 1)
    private BigDecimal studentSatisfactionScore;

    @Column(name = "course_health_score", precision = 5, scale = 2)
    private BigDecimal courseHealthScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", length = 50)
    private HealthStatus healthStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "critical_issues")
    private JsonNode criticalIssues;

    @Column(name = "recommendations")
    private String recommendations;

    @Column(name = "last_assessment_by")
    private UUID lastAssessmentBy;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
