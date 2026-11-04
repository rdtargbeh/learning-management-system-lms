package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.net.InetAddress;
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
 * Table <b>account_lock</b> (Identity, Accounts & Profiles domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "account_lock", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AccountLock implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "lock_id", updatable = false)
    private UUID lockId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Column(name = "lock_reason", length = 255)
    private String lockReason;

    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "last_failed_login_ip")
    private InetAddress lastFailedLoginIp;

    @Column(name = "date_locked")
    private OffsetDateTime dateLocked;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "date_unlocked")
    private OffsetDateTime dateUnlocked;

    @Column(name = "locked_by")
    private UUID lockedBy;

    @Column(name = "unlocked_by")
    private UUID unlockedBy;

    @Column(name = "unlock_token_hash", length = 255)
    private String unlockTokenHash;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
