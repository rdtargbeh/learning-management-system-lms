package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.Frequency;
import learning_management_system.backend.enums.TutorAssignmentStatus;
import learning_management_system.backend.enums.TutoringType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>tutor_assignment</b> (Academic Advising & Student Support domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "tutor_assignment", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class TutorAssignment implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "assignment_id", updatable = false)
    private UUID assignmentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "tutor_id", nullable = false)
    private UUID tutorId;

    @Column(name = "advisor_id")
    private UUID advisorId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "subject_area", length = 100)
    private String subjectArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "tutoring_type", nullable = false, length = 50)
    private TutoringType tutoringType;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 50)
    private Frequency frequency;

    @Column(name = "sessions_scheduled")
    private Integer sessionsScheduled;

    @Column(name = "sessions_completed")
    private Integer sessionsCompleted;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status", nullable = false, length = 50)
    private TutorAssignmentStatus assignmentStatus;

    @Column(name = "focus_areas")
    private String focusAreas;

    @Column(name = "student_goals")
    private String studentGoals;

    @Column(name = "assignment_notes")
    private String assignmentNotes;

    @Column(name = "effectiveness_rating", precision = 3, scale = 1)
    private BigDecimal effectivenessRating;

    @Column(name = "effectiveness_notes")
    private String effectivenessNotes;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
