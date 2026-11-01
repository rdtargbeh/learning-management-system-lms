package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ConferenceSessionProvider;
import learning_management_system.backend.enums.ConferenceStatus;
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
 * Table <b>conference_session</b> (Calendar & Attendance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "conference_session", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ConferenceSession implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "conference_id", updatable = false)
    private UUID conferenceId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "host_user_id")
    private UUID hostUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private ConferenceSessionProvider provider;

    @Column(name = "provider_meeting_id", length = 255)
    private String providerMeetingId;

    @Column(name = "conference_title", nullable = false, length = 255)
    private String conferenceTitle;

    @Column(name = "join_url", length = 1000)
    private String joinUrl;

    @Column(name = "host_url", length = 1000)
    private String hostUrl;

    @Column(name = "date_start")
    private OffsetDateTime dateStart;

    @Column(name = "date_end")
    private OffsetDateTime dateEnd;

    @Column(name = "recording_file_id")
    private UUID recordingFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "conference_status", nullable = false, length = 20)
    private ConferenceStatus conferenceStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private JsonNode metadata;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
