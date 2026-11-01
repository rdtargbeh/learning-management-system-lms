package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>attendance_record</b> (Calendar & Attendance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "attendance_record", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AttendanceRecord implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "record_id", updatable = false)
    private UUID recordId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "total_sessions")
    private Integer totalSessions;

    @Column(name = "total_present")
    private Integer totalPresent;

    @Column(name = "total_absent")
    private Integer totalAbsent;

    @Column(name = "total_late")
    private Integer totalLate;

    @Column(name = "total_excused")
    private Integer totalExcused;

    @Column(name = "total_unmarked")
    private Integer totalUnmarked;

    @Column(name = "attendance_percentage", precision = 5, scale = 2)
    private BigDecimal attendancePercentage;

    @Column(name = "on_track")
    private Boolean onTrack;

    @Column(name = "last_attendance_date")
    private LocalDate lastAttendanceDate;

    @Column(name = "last_absence_date")
    private LocalDate lastAbsenceDate;

    @Column(name = "consecutive_absences")
    private Integer consecutiveAbsences;

    @Column(name = "requires_intervention")
    private Boolean requiresIntervention;

    @Column(name = "intervention_notes")
    private String interventionNotes;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
