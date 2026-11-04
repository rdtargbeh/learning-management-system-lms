package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.CourseKind;
import learning_management_system.backend.enums.CourseLevel;
import learning_management_system.backend.enums.CourseStatus;
import learning_management_system.backend.enums.CourseType;
import learning_management_system.backend.enums.DefaultPostPolicy;
import learning_management_system.backend.enums.GradeCalculationMethod;
import learning_management_system.backend.enums.HomePageType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>course</b> (Academic Structure domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "course", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Course implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "course_id", updatable = false)
    private UUID courseId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "org_unit_id")
    private UUID orgUnitId;

    @Column(name = "term_id")
    private UUID termId;

    @Column(name = "course_code", nullable = false, length = 50)
    private String courseCode;

    @Column(name = "sis_source_id", length = 255)
    private String sisSourceId;

    @Column(name = "course_title", nullable = false, length = 255)
    private String courseTitle;

    @Column(name = "course_description")
    private String courseDescription;

    @Column(name = "syllabus_html")
    private String syllabusHtml;

    @Column(name = "credit_hours", precision = 3, scale = 1)
    private BigDecimal creditHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_level", length = 50)
    private CourseLevel courseLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", length = 50)
    private CourseType courseType;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_status", nullable = false, length = 20)
    private CourseStatus courseStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_kind", nullable = false, length = 20)
    private CourseKind courseKind;

    @Column(name = "date_start")
    private OffsetDateTime dateStart;

    @Column(name = "date_end")
    private OffsetDateTime dateEnd;

    @Column(name = "time_zone", length = 50)
    private String timeZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "home_page_type", nullable = false, length = 20)
    private HomePageType homePageType;

    @Column(name = "course_image_url", length = 1000)
    private String courseImageUrl;

    @Column(name = "allow_self_enrollment", nullable = false)
    private Boolean allowSelfEnrollment;

    @Column(name = "self_enrollment_code", length = 50)
    private String selfEnrollmentCode;

    @Column(name = "is_public_syllabus", nullable = false)
    private Boolean isPublicSyllabus;

    @Column(name = "content_license", length = 50)
    private String contentLicense;

    @Column(name = "grading_scale_id")
    private UUID gradingScaleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_calculation_method", nullable = false, length = 20)
    private GradeCalculationMethod gradeCalculationMethod;

    @Column(name = "final_grade_override_enabled", nullable = false)
    private Boolean finalGradeOverrideEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_post_policy", nullable = false, length = 10)
    private DefaultPostPolicy defaultPostPolicy;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
