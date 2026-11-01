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
 * Table <b>tenant_user_limit</b> (Tenancy & Institution Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "tenant_user_limit", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class TenantUserLimit implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "limit_id", updatable = false)
    private UUID limitId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "max_instructors")
    private Integer maxInstructors;

    @Column(name = "max_staff")
    private Integer maxStaff;

    @Column(name = "max_admins")
    private Integer maxAdmins;

    @Column(name = "current_students")
    private Integer currentStudents;

    @Column(name = "current_instructors")
    private Integer currentInstructors;

    @Column(name = "current_staff")
    private Integer currentStaff;

    @Column(name = "current_admins")
    private Integer currentAdmins;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
