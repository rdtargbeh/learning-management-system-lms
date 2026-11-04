package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AccessStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>platform_support_access</b> (Identity, Accounts & Profiles domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "platform_support_access", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class PlatformSupportAccess implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "support_access_id", updatable = false)
    private UUID supportAccessId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "platform_user_id", nullable = false)
    private UUID platformUserId;

    @Column(name = "impersonated_user_id")
    private UUID impersonatedUserId;

    @Column(name = "access_reason", nullable = false)
    private String accessReason;

    @Column(name = "ticket_reference", length = 100)
    private String ticketReference;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_status", nullable = false, length = 20)
    private AccessStatus accessStatus;

    @Column(name = "date_start")
    private OffsetDateTime dateStart;

    @Column(name = "date_expires", nullable = false)
    private OffsetDateTime dateExpires;

    @Column(name = "date_ended")
    private OffsetDateTime dateEnded;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
