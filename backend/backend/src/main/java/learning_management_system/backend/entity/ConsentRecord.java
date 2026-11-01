package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ConsentType;
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
 * Table <b>consent_record</b> (Privacy & Records (FERPA / GDPR) domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "consent_record", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ConsentRecord implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "consent_id", updatable = false)
    private UUID consentId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "policy_id")
    private UUID policyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false, length = 50)
    private ConsentType consentType;

    @Column(name = "consent_version", nullable = false, length = 50)
    private String consentVersion;

    @Column(name = "is_granted", nullable = false)
    private Boolean isGranted;

    @Column(name = "date_granted")
    private OffsetDateTime dateGranted;

    @Column(name = "date_revoked")
    private OffsetDateTime dateRevoked;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
