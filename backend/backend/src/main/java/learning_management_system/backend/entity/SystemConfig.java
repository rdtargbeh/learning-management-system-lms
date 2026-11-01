package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ConfigCategory;
import learning_management_system.backend.enums.ConfigType;
import learning_management_system.backend.enums.Environment;
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
 * Table <b>system_config</b> (System Administration & Maintenance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "system_config", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class SystemConfig implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "config_id", updatable = false)
    private UUID configId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_category", nullable = false, length = 100)
    private ConfigCategory configCategory;

    @Column(name = "config_key", nullable = false, length = 255)
    private String configKey;

    @Column(name = "config_value")
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false, length = 50)
    private ConfigType configType;

    @Column(name = "description")
    private String description;

    @Column(name = "is_sensitive")
    private Boolean isSensitive;

    @Column(name = "is_encrypted")
    private Boolean isEncrypted;

    @Column(name = "default_value")
    private String defaultValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_values")
    private JsonNode allowedValues;

    @Column(name = "validation_rule", length = 255)
    private String validationRule;

    @Column(name = "min_value")
    private BigDecimal minValue;

    @Column(name = "max_value")
    private BigDecimal maxValue;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "modified_by")
    private UUID modifiedBy;

    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;

    @Column(name = "applied_at")
    private OffsetDateTime appliedAt;

    @Column(name = "requires_restart")
    private Boolean requiresRestart;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", length = 50)
    private Environment environment;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
