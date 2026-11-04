package learning_management_system.backend.entity;

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

/**
 * Table <b>tenant_settings</b> (Tenancy & Institution Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "tenant_settings", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class TenantSettings implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "settings_id", updatable = false)
    private UUID settingsId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "theme_color", length = 7)
    private String themeColor;

    @Column(name = "banner_logo_url", length = 500)
    private String bannerLogoUrl;

    @Column(name = "support_email", length = 255)
    private String supportEmail;

    @Column(name = "from_email", length = 255)
    private String fromEmail;

    @Column(name = "max_upload_size_mb")
    private Integer maxUploadSizeMb;

    @Column(name = "session_timeout_minutes")
    private Integer sessionTimeoutMinutes;

    @Column(name = "password_min_length")
    private Integer passwordMinLength;

    @Column(name = "require_mfa")
    private Boolean requireMfa;

    @Column(name = "allow_self_registration")
    private Boolean allowSelfRegistration;

    @Column(name = "maintenance_mode")
    private Boolean maintenanceMode;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
