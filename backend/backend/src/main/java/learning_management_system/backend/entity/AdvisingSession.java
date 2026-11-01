package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AdvisingSessionStatus;
import learning_management_system.backend.enums.SessionType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>advising_session</b> (Academic Advising & Student Support domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "advising_session", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AdvisingSession implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id", updatable = false)
    private UUID sessionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "advisor_id", nullable = false)
    private UUID advisorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 50)
    private SessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false, length = 50)
    private AdvisingSessionStatus sessionStatus;

    @Column(name = "session_title", length = 255)
    private String sessionTitle;

    @Column(name = "session_description")
    private String sessionDescription;

    @Column(name = "session_notes")
    private String sessionNotes;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "session_start_time")
    private LocalTime sessionStartTime;

    @Column(name = "session_end_time")
    private LocalTime sessionEndTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "is_online")
    private Boolean isOnline;

    @Column(name = "meeting_url", length = 500)
    private String meetingUrl;

    @Column(name = "is_mandatory")
    private Boolean isMandatory;

    @Column(name = "student_attended")
    private Boolean studentAttended;

    @Column(name = "date_attended")
    private OffsetDateTime dateAttended;

    @Column(name = "follow_up_required")
    private Boolean followUpRequired;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "topics_discussed")
    private JsonNode topicsDiscussed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_items")
    private JsonNode actionItems;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
