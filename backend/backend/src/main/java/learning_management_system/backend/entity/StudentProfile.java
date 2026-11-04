package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AcademicLevel;
import learning_management_system.backend.enums.AcademicStanding;
import learning_management_system.backend.enums.StudentProfileEnrollmentStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>student_profile</b> (Identity, Accounts & Profiles domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "student_profile", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class StudentProfile implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "student_profile_id", updatable = false)
    private UUID studentProfileId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "student_number", length = 50)
    private String studentNumber;

    @Column(name = "org_unit_id")
    private UUID orgUnitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_level", length = 30)
    private AcademicLevel academicLevel;

    @Column(name = "program_name", length = 150)
    private String programName;

    @Column(name = "major", length = 100)
    private String major;

    @Column(name = "minor", length = 100)
    private String minor;

    @Column(name = "concentration", length = 100)
    private String concentration;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status", nullable = false, length = 30)
    private StudentProfileEnrollmentStatus enrollmentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_standing", length = 30)
    private AcademicStanding academicStanding;

    @Column(name = "is_full_time", nullable = false)
    private Boolean isFullTime;

    @Column(name = "cumulative_gpa", precision = 4, scale = 3)
    private BigDecimal cumulativeGpa;

    @Column(name = "total_credits_completed", nullable = false, precision = 6, scale = 1)
    private BigDecimal totalCreditsCompleted;

    @Column(name = "total_credits_required", precision = 6, scale = 1)
    private BigDecimal totalCreditsRequired;

    @Column(name = "date_admitted")
    private LocalDate dateAdmitted;

    @Column(name = "admit_term_id")
    private UUID admitTermId;

    @Column(name = "expected_graduation_term_id")
    private UUID expectedGraduationTermId;

    @Column(name = "date_graduated")
    private LocalDate dateGraduated;

    @Column(name = "advisor_user_id")
    private UUID advisorUserId;

    @Column(name = "ferpa_directory_hold", nullable = false)
    private Boolean ferpaDirectoryHold;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
