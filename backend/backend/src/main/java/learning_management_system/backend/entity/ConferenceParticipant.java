package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ConferenceParticipantRole;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Table <b>conference_participant</b> (Calendar & Attendance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "conference_participant", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ConferenceParticipant implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "conference_participant_id", updatable = false)
    private UUID conferenceParticipantId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "conference_id", nullable = false)
    private UUID conferenceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_role", nullable = false, length = 20)
    private ConferenceParticipantRole participantRole;

    @Column(name = "date_joined", nullable = false)
    private OffsetDateTime dateJoined;

    @Column(name = "date_left")
    private OffsetDateTime dateLeft;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}
