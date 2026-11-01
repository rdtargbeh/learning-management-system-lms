package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.EnrollmentSource;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.EnrollmentType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>enrollment</b> (Enrollment Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "enrollment", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Enrollment implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "enrollment_id", updatable = false)
    private UUID enrollmentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_type", length = 50)
    private EnrollmentType enrollmentType;

    @Column(name = "observed_user_id")
    private UUID observedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status", nullable = false, length = 50)
    private EnrollmentStatus enrollmentStatus;

    @Column(name = "sis_source_id", length = 255)
    private String sisSourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_source", nullable = false, length = 20)
    private EnrollmentSource enrollmentSource;

    @Column(name = "limit_to_section", nullable = false)
    private Boolean limitToSection;

    @Column(name = "date_enrolled", nullable = false)
    private LocalDate dateEnrolled;

    @Column(name = "date_started")
    private LocalDate dateStarted;

    @Column(name = "date_withdrawn")
    private LocalDate dateWithdrawn;

    @Column(name = "date_completed")
    private LocalDate dateCompleted;

    @Column(name = "date_last_activity")
    private OffsetDateTime dateLastActivity;

    @Column(name = "total_activity_seconds", nullable = false)
    private Long totalActivitySeconds;

    @Column(name = "is_credit_bearing")
    private Boolean isCreditBearing;

    @Column(name = "is_graded")
    private Boolean isGraded;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
