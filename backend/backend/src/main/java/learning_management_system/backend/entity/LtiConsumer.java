package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LaunchPrivacyLevel;
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
 * Table <b>lti_consumer</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "lti_consumer", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class LtiConsumer implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "consumer_id", updatable = false)
    private UUID consumerId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "external_tool_id", nullable = false)
    private UUID externalToolId;

    @Column(name = "consumer_key", nullable = false, length = 255)
    private String consumerKey;

    @Column(name = "consumer_secret", nullable = false, length = 500)
    private String consumerSecret;

    @Column(name = "consumer_version", nullable = false, length = 50)
    private String consumerVersion;

    @Column(name = "consumer_name", length = 255)
    private String consumerName;

    @Column(name = "consumer_guid", length = 255)
    private String consumerGuid;

    @Column(name = "launch_url", nullable = false, length = 500)
    private String launchUrl;

    @Column(name = "content_item_selection_url", length = 500)
    private String contentItemSelectionUrl;

    @Column(name = "tool_consumer_info_url", length = 500)
    private String toolConsumerInfoUrl;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "oauth_token_endpoint", length = 500)
    private String oauthTokenEndpoint;

    @Column(name = "oauth_authorization_endpoint", length = 500)
    private String oauthAuthorizationEndpoint;

    @Column(name = "oauth_client_id", length = 255)
    private String oauthClientId;

    @Column(name = "oauth_client_secret", length = 500)
    private String oauthClientSecret;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "supported_lti_versions")
    private JsonNode supportedLtiVersions;

    @Enumerated(EnumType.STRING)
    @Column(name = "launch_privacy_level", length = 50)
    private LaunchPrivacyLevel launchPrivacyLevel;

    @Column(name = "enable_tool_state")
    private Boolean enableToolState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "domain_whitelist")
    private JsonNode domainWhitelist;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_parameters")
    private JsonNode customParameters;

    @Column(name = "last_launch")
    private OffsetDateTime lastLaunch;

    @Column(name = "launch_count")
    private Integer launchCount;

    @Column(name = "error_count")
    private Integer errorCount;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
