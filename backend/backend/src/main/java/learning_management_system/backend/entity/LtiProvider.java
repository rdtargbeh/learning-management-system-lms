package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
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
 * Table <b>lti_provider</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "lti_provider", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class LtiProvider implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "provider_id", updatable = false)
    private UUID providerId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "provider_name", nullable = false, length = 255)
    private String providerName;

    @Column(name = "provider_description")
    private String providerDescription;

    @Column(name = "provider_key", nullable = false, length = 255)
    private String providerKey;

    @Column(name = "provider_secret", nullable = false, length = 500)
    private String providerSecret;

    @Column(name = "provider_version", nullable = false, length = 50)
    private String providerVersion;

    @Column(name = "provider_url", length = 500)
    private String providerUrl;

    @Column(name = "provider_icon_url", length = 500)
    private String providerIconUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "allow_account_creation")
    private Boolean allowAccountCreation;

    @Column(name = "auto_activate_accounts")
    private Boolean autoActivateAccounts;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "supported_extensions")
    private JsonNode supportedExtensions;

    @Column(name = "grade_passback_enabled")
    private Boolean gradePassbackEnabled;

    @Column(name = "outcome_service_url", length = 500)
    private String outcomeServiceUrl;

    @Column(name = "result_sourceid_prefix", length = 50)
    private String resultSourceidPrefix;

    @Column(name = "unique_sourceid_per_context")
    private Boolean uniqueSourceidPerContext;

    @Column(name = "max_active_contexts")
    private Integer maxActiveContexts;

    @Column(name = "current_active_contexts")
    private Integer currentActiveContexts;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ip_whitelist")
    private JsonNode ipWhitelist;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "oauth_settings")
    private JsonNode oauthSettings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_parameters")
    private JsonNode customParameters;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
