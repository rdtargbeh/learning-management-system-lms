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
 * Table <b>email_verification_token</b> (Identity, Accounts & Profiles domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "email_verification_token", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class EmailVerificationToken implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "token_id", updatable = false)
    private UUID tokenId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "email_to_verify", nullable = false, length = 255)
    private String emailToVerify;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "request_ip")
    private InetAddress requestIp;

    @Column(name = "request_user_agent")
    private String requestUserAgent;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Column(name = "attempt_count")
    private Integer attemptCount;

    @Column(name = "resend_count")
    private Integer resendCount;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;
}
