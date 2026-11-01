package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.BlueprintContentLockObjectType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>blueprint_content_lock</b> (Academic Structure domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "blueprint_content_lock", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class BlueprintContentLock implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "lock_id", updatable = false)
    private UUID lockId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "blueprint_course_id", nullable = false)
    private UUID blueprintCourseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false, length = 30)
    private BlueprintContentLockObjectType objectType;

    @Column(name = "object_id", nullable = false)
    private UUID objectId;

    @Column(name = "lock_content", nullable = false)
    private Boolean lockContent;

    @Column(name = "lock_points", nullable = false)
    private Boolean lockPoints;

    @Column(name = "lock_due_dates", nullable = false)
    private Boolean lockDueDates;

    @Column(name = "lock_availability_dates", nullable = false)
    private Boolean lockAvailabilityDates;

    @Column(name = "lock_settings", nullable = false)
    private Boolean lockSettings;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
