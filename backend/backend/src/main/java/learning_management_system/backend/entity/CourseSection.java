package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.CourseSectionStatus;
import learning_management_system.backend.enums.DeliveryMode;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>course_section</b> (Academic Structure domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "course_section", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class CourseSection implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "section_id", updatable = false)
    private UUID sectionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "crosslisted_from_course_id")
    private UUID crosslistedFromCourseId;

    @Column(name = "term_id")
    private UUID termId;

    @Column(name = "section_number", length = 10)
    private String sectionNumber;

    @Column(name = "sis_source_id", length = 255)
    private String sisSourceId;

    @Column(name = "capacity")
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_mode", length = 20)
    private DeliveryMode deliveryMode;

    @Column(name = "enrolled_count")
    private Integer enrolledCount;

    @Column(name = "waitlist_count")
    private Integer waitlistCount;

    @Column(name = "date_start")
    private LocalDate dateStart;

    @Column(name = "date_end")
    private LocalDate dateEnd;

    @Column(name = "registration_open_date")
    private LocalDate registrationOpenDate;

    @Column(name = "registration_close_date")
    private LocalDate registrationCloseDate;

    @Column(name = "is_open_for_enrollment")
    private Boolean isOpenForEnrollment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private CourseSectionStatus status;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
