package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.StudentAttendanceStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>student_attendance</b> (Calendar & Attendance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "student_attendance", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class StudentAttendance implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "attendance_id", updatable = false)
    private UUID attendanceId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 50)
    private StudentAttendanceStatus attendanceStatus;

    @Column(name = "marked_time")
    private OffsetDateTime markedTime;

    @Column(name = "marked_by_id")
    private UUID markedById;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "is_late")
    private Boolean isLate;

    @Column(name = "late_minutes")
    private Integer lateMinutes;

    @Column(name = "is_excused")
    private Boolean isExcused;

    @Column(name = "excused_reason", length = 255)
    private String excusedReason;

    @Column(name = "excused_by_id")
    private UUID excusedById;

    @Column(name = "excused_until")
    private LocalDate excusedUntil;

    @Column(name = "notes")
    private String notes;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
