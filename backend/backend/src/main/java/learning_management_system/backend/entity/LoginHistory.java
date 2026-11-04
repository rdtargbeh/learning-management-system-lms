package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LoginMethod;
import learning_management_system.backend.enums.LoginStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>login_history</b> (Identity, Accounts & Profiles domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "login_history", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class LoginHistory implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "login_id", updatable = false)
    private UUID loginId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_status", nullable = false, length = 50)
    private LoginStatus loginStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_method", nullable = false, length = 50)
    private LoginMethod loginMethod;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "mfa_used")
    private Boolean mfaUsed;

    @Column(name = "is_suspicious")
    private Boolean isSuspicious;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "login_timestamp", nullable = false)
    private OffsetDateTime loginTimestamp;
}
