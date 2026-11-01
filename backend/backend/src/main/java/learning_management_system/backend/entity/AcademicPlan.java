package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AcademicPlanStatus;
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
 * Table <b>academic_plan</b> (Academic Advising & Student Support domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "academic_plan", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AcademicPlan implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "plan_id", updatable = false)
    private UUID planId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "advisor_id", nullable = false)
    private UUID advisorId;

    @Column(name = "degree_id")
    private UUID degreeId;

    @Column(name = "major_id")
    private UUID majorId;

    @Column(name = "minor_id")
    private UUID minorId;

    @Column(name = "concentration_id")
    private UUID concentrationId;

    @Column(name = "plan_name", length = 255)
    private String planName;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_status", nullable = false, length = 50)
    private AcademicPlanStatus planStatus;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "approved_date")
    private LocalDate approvedDate;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "total_credits_required")
    private Integer totalCreditsRequired;

    @Column(name = "credits_completed")
    private Integer creditsCompleted;

    @Column(name = "gpa_requirement", precision = 3, scale = 2)
    private BigDecimal gpaRequirement;

    @Column(name = "completion_target_date")
    private LocalDate completionTargetDate;

    @Column(name = "expected_graduation_semester", length = 20)
    private String expectedGraduationSemester;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "plan_content")
    private JsonNode planContent;

    @Column(name = "curriculum_version", length = 20)
    private String curriculumVersion;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
