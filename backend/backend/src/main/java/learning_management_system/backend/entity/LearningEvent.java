package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LearningEventResourceType;
import learning_management_system.backend.enums.LearningEventType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>learning_event</b> (Content Delivery & Learning Activity domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "learning_event", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class LearningEvent implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", updatable = false)
    private UUID eventId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "stud_session_id", nullable = false)
    private UUID studSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private LearningEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", length = 50)
    private LearningEventResourceType resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_details")
    private JsonNode actionDetails;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "date_event", nullable = false)
    private OffsetDateTime dateEvent;
}
