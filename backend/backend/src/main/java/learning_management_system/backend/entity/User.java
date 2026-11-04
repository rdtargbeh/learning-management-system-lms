package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.AuthProvider;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>users</b> (Identity, Accounts & Profiles domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "users", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class User implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false)
    private UUID userId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "is_system_user", nullable = false)
    private Boolean isSystemUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 30)
    private UserType userType;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "email", nullable = false, columnDefinition = "citext")
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 30)
    private AccountStatus accountStatus;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    @Column(name = "date_email_verified")
    private OffsetDateTime dateEmailVerified;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 50)
    private AuthProvider authProvider;

    @Column(name = "external_auth_id", length = 255)
    private String externalAuthId;

    @Column(name = "sis_source_id", length = 255)
    private String sisSourceId;

    @Column(name = "mfa_required", nullable = false)
    private Boolean mfaRequired;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "login_count", nullable = false)
    private Integer loginCount;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts;

    @Column(name = "last_failed_login")
    private OffsetDateTime lastFailedLogin;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Column(name = "account_locked_until")
    private OffsetDateTime accountLockedUntil;

    @Column(name = "password_last_changed")
    private OffsetDateTime passwordLastChanged;

    @Column(name = "password_expires_at")
    private OffsetDateTime passwordExpiresAt;

    @Column(name = "must_change_password", nullable = false)
    private Boolean mustChangePassword;

    @Column(name = "last_password_reset")
    private OffsetDateTime lastPasswordReset;

    @Column(name = "is_test_user", nullable = false)
    private Boolean isTestUser;

    @Column(name = "calendar_feed_token_hash", length = 255)
    private String calendarFeedTokenHash;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "date_deactivated")
    private OffsetDateTime dateDeactivated;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
