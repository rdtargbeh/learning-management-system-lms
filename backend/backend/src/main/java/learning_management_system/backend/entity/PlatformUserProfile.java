package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.PlatformRole;
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
 * Table <b>platform_user_profile</b> (Identity, Accounts & Profiles domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "platform_user_profile", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class PlatformUserProfile implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "platform_profile_id", updatable = false)
    private UUID platformProfileId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "is_system_user", nullable = false)
    private Boolean isSystemUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform_role", nullable = false, length = 30)
    private PlatformRole platformRole;

    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "employee_number", length = 50)
    private String employeeNumber;

    @Column(name = "can_impersonate", nullable = false)
    private Boolean canImpersonate;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "support_tenant_scope")
    private UUID[] supportTenantScope;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "ip_allowlist")
    private InetAddress[] ipAllowlist;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "date_last_access")
    private OffsetDateTime dateLastAccess;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
