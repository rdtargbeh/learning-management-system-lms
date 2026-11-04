package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.PrivacyLevel;
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
 * Table <b>lti_registration</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "lti_registration", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class LtiRegistration implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "registration_id", updatable = false)
    private UUID registrationId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "external_tool_id", nullable = false)
    private UUID externalToolId;

    @Column(name = "lti_version", nullable = false, length = 10)
    private String ltiVersion;

    @Column(name = "issuer", nullable = false, length = 500)
    private String issuer;

    @Column(name = "client_id", nullable = false, length = 255)
    private String clientId;

    @Column(name = "deployment_id", nullable = false, length = 255)
    private String deploymentId;

    @Column(name = "oidc_login_url", nullable = false, length = 1000)
    private String oidcLoginUrl;

    @Column(name = "target_link_uri", nullable = false, length = 1000)
    private String targetLinkUri;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "redirect_uris")
    private JsonNode redirectUris;

    @Column(name = "tool_jwks_url", length = 1000)
    private String toolJwksUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tool_public_jwk")
    private JsonNode toolPublicJwk;

    @Column(name = "platform_key_id", length = 255)
    private String platformKeyId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scopes")
    private JsonNode scopes;

    @Column(name = "ags_enabled", nullable = false)
    private Boolean agsEnabled;

    @Column(name = "nrps_enabled", nullable = false)
    private Boolean nrpsEnabled;

    @Column(name = "deep_linking_enabled", nullable = false)
    private Boolean deepLinkingEnabled;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "placements")
    private JsonNode placements;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_level", nullable = false, length = 20)
    private PrivacyLevel privacyLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_parameters")
    private JsonNode customParameters;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
