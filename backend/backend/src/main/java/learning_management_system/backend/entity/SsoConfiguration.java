package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.SsoProvider;
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
 * Table <b>sso_configuration</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "sso_configuration", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class SsoConfiguration implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "config_id", updatable = false)
    private UUID configId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sso_provider", nullable = false, length = 50)
    private SsoProvider ssoProvider;

    @Column(name = "provider_name", length = 255)
    private String providerName;

    @Column(name = "is_primary_sso")
    private Boolean isPrimarySso;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "auto_provision_users")
    private Boolean autoProvisionUsers;

    @Column(name = "auto_activate_accounts")
    private Boolean autoActivateAccounts;

    @Column(name = "just_in_time_provisioning")
    private Boolean justInTimeProvisioning;

    @Column(name = "sync_profile_data")
    private Boolean syncProfileData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sync_user_attributes")
    private JsonNode syncUserAttributes;

    @Column(name = "create_unknown_users")
    private Boolean createUnknownUsers;

    @Column(name = "update_user_email")
    private Boolean updateUserEmail;

    @Column(name = "case_sensitive_username")
    private Boolean caseSensitiveUsername;

    @Column(name = "username_normalization", length = 50)
    private String usernameNormalization;

    @Column(name = "default_user_role", length = 100)
    private String defaultUserRole;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration_data", nullable = false)
    private JsonNode configurationData;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Column(name = "certificate_thumbprint", length = 255)
    private String certificateThumbprint;

    @Column(name = "encryption_enabled")
    private Boolean encryptionEnabled;

    @Column(name = "signing_enabled")
    private Boolean signingEnabled;

    @Column(name = "metadata_url", length = 500)
    private String metadataUrl;

    @Column(name = "entity_id", length = 500)
    private String entityId;

    @Column(name = "acs_url", length = 500)
    private String acsUrl;

    @Column(name = "sso_login_url", length = 500)
    private String ssoLoginUrl;

    @Column(name = "sso_logout_url", length = 500)
    private String ssoLogoutUrl;

    @Column(name = "idp_issuer", length = 255)
    private String idpIssuer;

    @Column(name = "assertion_consumer_service_url", length = 500)
    private String assertionConsumerServiceUrl;

    @Column(name = "single_logout_service_url", length = 500)
    private String singleLogoutServiceUrl;

    @Column(name = "nameid_format", length = 100)
    private String nameidFormat;

    @Column(name = "date_tested")
    private OffsetDateTime dateTested;

    @Column(name = "last_sync")
    private OffsetDateTime lastSync;

    @Column(name = "sync_count")
    private Integer syncCount;

    @Column(name = "sync_error_count")
    private Integer syncErrorCount;

    @Column(name = "last_error_message")
    private String lastErrorMessage;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
