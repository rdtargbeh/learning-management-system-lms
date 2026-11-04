package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.RegistrationStatus;
import learning_management_system.backend.enums.RsvpStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>event_participant</b> (Calendar & Attendance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "event_participant", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class EventParticipant implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "participant_id", updatable = false)
    private UUID participantId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", nullable = false, length = 50)
    private RegistrationStatus registrationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "rsvp_status", length = 50)
    private RsvpStatus rsvpStatus;

    @Column(name = "registered_at", nullable = false)
    private OffsetDateTime registeredAt;

    @Column(name = "registered_by_id")
    private UUID registeredById;

    @Column(name = "attended")
    private Boolean attended;

    @Column(name = "attendance_marked_at")
    private OffsetDateTime attendanceMarkedAt;

    @Column(name = "marked_by_id")
    private UUID markedById;

    @Column(name = "attendance_late")
    private Boolean attendanceLate;

    @Column(name = "late_minutes")
    private Integer lateMinutes;

    @Column(name = "notes")
    private String notes;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
