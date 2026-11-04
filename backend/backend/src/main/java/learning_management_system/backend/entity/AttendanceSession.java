package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AttendanceMethod;
import learning_management_system.backend.enums.AttendanceSessionAttendanceStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>attendance_session</b> (Calendar & Attendance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "attendance_session", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AttendanceSession implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id", updatable = false)
    private UUID sessionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "session_start_time")
    private LocalTime sessionStartTime;

    @Column(name = "session_end_time")
    private LocalTime sessionEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 50)
    private AttendanceSessionAttendanceStatus attendanceStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_method", nullable = false, length = 50)
    private AttendanceMethod attendanceMethod;

    @Column(name = "attendance_code", length = 10)
    private String attendanceCode;

    @Column(name = "attendance_code_expires_at")
    private OffsetDateTime attendanceCodeExpiresAt;

    @Column(name = "is_locked")
    private Boolean isLocked;

    @Column(name = "locked_at")
    private OffsetDateTime lockedAt;

    @Column(name = "locked_by_id")
    private UUID lockedById;

    @Column(name = "total_enrolled")
    private Integer totalEnrolled;

    @Column(name = "total_present")
    private Integer totalPresent;

    @Column(name = "total_absent")
    private Integer totalAbsent;

    @Column(name = "total_late")
    private Integer totalLate;

    @Column(name = "total_excused")
    private Integer totalExcused;

    @Column(name = "notes")
    private String notes;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
