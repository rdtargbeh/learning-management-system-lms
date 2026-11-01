package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AuthenticationType;
import learning_management_system.backend.enums.ConfigurationStatus;
import learning_management_system.backend.enums.InstallationStatus;
import learning_management_system.backend.enums.ToolType;
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
 * Table <b>external_tool</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "external_tool", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ExternalTool implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tool_id", updatable = false)
    private UUID toolId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "tool_name", nullable = false, length = 255)
    private String toolName;

    @Column(name = "tool_description")
    private String toolDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "tool_type", nullable = false, length = 50)
    private ToolType toolType;

    @Column(name = "tool_provider", length = 100)
    private String toolProvider;

    @Column(name = "tool_url", length = 500)
    private String toolUrl;

    @Column(name = "tool_icon_url", length = 500)
    private String toolIconUrl;

    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "authentication_type", length = 50)
    private AuthenticationType authenticationType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "approved_date")
    private OffsetDateTime approvedDate;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "installation_status", length = 50)
    private InstallationStatus installationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "configuration_status", length = 50)
    private ConfigurationStatus configurationStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "enabled_features")
    private JsonNode enabledFeatures;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_scopes")
    private JsonNode requiredScopes;

    @Column(name = "rate_limit_calls")
    private Integer rateLimitCalls;

    @Column(name = "rate_limit_period", length = 50)
    private String rateLimitPeriod;

    @Column(name = "support_contact", length = 255)
    private String supportContact;

    @Column(name = "documentation_url", length = 500)
    private String documentationUrl;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
