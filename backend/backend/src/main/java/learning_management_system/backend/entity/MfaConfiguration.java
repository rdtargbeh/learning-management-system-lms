package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.MfaType;
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
 * Table <b>mfa_configuration</b> (Identity, Accounts & Profiles domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "mfa_configuration", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class MfaConfiguration implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "mfa_id", updatable = false)
    private UUID mfaId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "mfa_type", length = 50)
    private MfaType mfaType;

    @Column(name = "totp_secret_encrypted", length = 500)
    private String totpSecretEncrypted;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "backup_codes")
    private JsonNode backupCodes;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "hardware_key_id", length = 255)
    private String hardwareKeyId;

    @Column(name = "last_mfa_at")
    private OffsetDateTime lastMfaAt;

    @Column(name = "failed_mfa_attempts")
    private Integer failedMfaAttempts;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
