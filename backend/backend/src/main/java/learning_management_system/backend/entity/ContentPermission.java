package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.PermissionType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Table <b>content_permission</b> (Learning Content Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "content_permission", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ContentPermission implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "permission_id", updatable = false)
    private UUID permissionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false, length = 50)
    private PermissionType permissionType;

    @Column(name = "is_granted", nullable = false)
    private Boolean isGranted;

    @Column(name = "date_granted", nullable = false)
    private OffsetDateTime dateGranted;

    @Column(name = "date_expires")
    private OffsetDateTime dateExpires;

    @Column(name = "granted_by")
    private UUID grantedBy;
}
