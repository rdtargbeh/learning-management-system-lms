package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.IntegrationType;
import learning_management_system.backend.enums.LogAction;
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
 * Table <b>integration_log</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "integration_log", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class IntegrationLog implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id", updatable = false)
    private UUID logId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type", nullable = false, length = 50)
    private IntegrationType integrationType;

    @Column(name = "tool_id")
    private UUID toolId;

    @Column(name = "api_key_id")
    private UUID apiKeyId;

    @Column(name = "webhook_id")
    private UUID webhookId;

    @Column(name = "sso_config_id")
    private UUID ssoConfigId;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_action", nullable = false, length = 50)
    private LogAction logAction;

    @Column(name = "source_system", length = 100)
    private String sourceSystem;

    @Column(name = "target_system", length = 100)
    private String targetSystem;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_data")
    private JsonNode requestData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_data")
    private JsonNode responseData;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "error_stack_trace")
    private String errorStackTrace;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "processed_records")
    private Integer processedRecords;

    @Column(name = "failed_records")
    private Integer failedRecords;

    @Column(name = "performed_by")
    private UUID performedBy;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
