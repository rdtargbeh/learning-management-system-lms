package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.FlagStatus;
import learning_management_system.backend.enums.FlagType;
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
 * Table <b>feature_flag</b> (System Administration & Maintenance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "feature_flag", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class FeatureFlag implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "flag_id", updatable = false)
    private UUID flagId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "flag_name", nullable = false, length = 255)
    private String flagName;

    @Column(name = "flag_description")
    private String flagDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", nullable = false, length = 50)
    private FlagType flagType;

    @Enumerated(EnumType.STRING)
    @Column(name = "flag_status", nullable = false, length = 50)
    private FlagStatus flagStatus;

    @Column(name = "feature_module", length = 100)
    private String featureModule;

    @Column(name = "is_beta")
    private Boolean isBeta;

    @Column(name = "rollout_percentage")
    private Integer rolloutPercentage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "enabled_for_users")
    private JsonNode enabledForUsers;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "enabled_for_groups")
    private JsonNode enabledForGroups;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "enabled_for_organizations")
    private JsonNode enabledForOrganizations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "enabled_for_environments")
    private JsonNode enabledForEnvironments;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "condition_rules")
    private JsonNode conditionRules;

    @Column(name = "default_value")
    private Boolean defaultValue;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

    @Column(name = "modified_by")
    private UUID modifiedBy;

    @Column(name = "modified_date")
    private OffsetDateTime modifiedDate;

    @Column(name = "activation_date")
    private OffsetDateTime activationDate;

    @Column(name = "deactivation_date")
    private OffsetDateTime deactivationDate;

    @Column(name = "sunset_date")
    private LocalDate sunsetDate;

    @Column(name = "deprecation_notice")
    private String deprecationNotice;

    @Column(name = "analytics_enabled")
    private Boolean analyticsEnabled;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "analytics_data")
    private JsonNode analyticsData;

    @Column(name = "notification_on_change")
    private Boolean notificationOnChange;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "related_feature_flags")
    private JsonNode relatedFeatureFlags;

    @Column(name = "release_notes_url", length = 500)
    private String releaseNotesUrl;

    @Column(name = "documentation_url", length = 500)
    private String documentationUrl;

    @Column(name = "is_global_flag")
    private Boolean isGlobalFlag;

    @Column(name = "flag_owner_id")
    private UUID flagOwnerId;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
