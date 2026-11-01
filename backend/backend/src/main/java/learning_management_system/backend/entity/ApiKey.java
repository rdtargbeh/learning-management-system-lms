package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.KeyType;
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
 * Table <b>api_key</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "api_key", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ApiKey implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "key_id", updatable = false)
    private UUID keyId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "key_name", nullable = false, length = 255)
    private String keyName;

    @Column(name = "key_description")
    private String keyDescription;

    @Column(name = "api_key_hash", nullable = false, length = 255)
    private String apiKeyHash;

    @Column(name = "api_secret_hash", length = 255)
    private String apiSecretHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false, length = 50)
    private KeyType keyType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scopes")
    private JsonNode scopes;

    @Column(name = "rate_limit_calls")
    private Integer rateLimitCalls;

    @Column(name = "rate_limit_period", length = 50)
    private String rateLimitPeriod;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_revoked")
    private Boolean isRevoked;

    @Column(name = "revoked_date")
    private OffsetDateTime revokedDate;

    @Column(name = "revoked_reason")
    private String revokedReason;

    @Column(name = "last_used")
    private OffsetDateTime lastUsed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ip_whitelist")
    private JsonNode ipWhitelist;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_endpoints")
    private JsonNode allowedEndpoints;

    @Column(name = "usage_count")
    private Integer usageCount;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "notification_email", length = 255)
    private String notificationEmail;

    @Column(name = "notify_expiration_days")
    private Integer notifyExpirationDays;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
