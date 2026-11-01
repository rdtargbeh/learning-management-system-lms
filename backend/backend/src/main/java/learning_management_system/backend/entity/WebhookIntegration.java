package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.DeliveryStatus;
import learning_management_system.backend.enums.RequestTemplate;
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
 * Table <b>webhook_integration</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "webhook_integration", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class WebhookIntegration implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "webhook_id", updatable = false)
    private UUID webhookId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "webhook_url", nullable = false, length = 500)
    private String webhookUrl;

    @Column(name = "webhook_name", length = 255)
    private String webhookName;

    @Column(name = "webhook_description")
    private String webhookDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_types", nullable = false)
    private JsonNode eventTypes;

    @Column(name = "webhook_secret", length = 500)
    private String webhookSecret;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "retry_enabled")
    private Boolean retryEnabled;

    @Column(name = "retry_max_attempts")
    private Integer retryMaxAttempts;

    @Column(name = "retry_backoff_seconds")
    private Integer retryBackoffSeconds;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", length = 50)
    private DeliveryStatus deliveryStatus;

    @Column(name = "last_delivery")
    private OffsetDateTime lastDelivery;

    @Column(name = "last_delivery_status", length = 50)
    private String lastDeliveryStatus;

    @Column(name = "successful_deliveries")
    private Integer successfulDeliveries;

    @Column(name = "failed_deliveries")
    private Integer failedDeliveries;

    @Column(name = "pending_deliveries")
    private Integer pendingDeliveries;

    @Column(name = "ssl_verify")
    private Boolean sslVerify;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_headers")
    private JsonNode customHeaders;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_template", length = 50)
    private RequestTemplate requestTemplate;

    @Column(name = "batching_enabled")
    private Boolean batchingEnabled;

    @Column(name = "batch_size")
    private Integer batchSize;

    @Column(name = "batch_timeout_seconds")
    private Integer batchTimeoutSeconds;

    @Column(name = "rate_limit_calls")
    private Integer rateLimitCalls;

    @Column(name = "rate_limit_period", length = 50)
    private String rateLimitPeriod;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
