package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
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
 * Table <b>student_group</b> (Enrollment Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "student_group", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class StudentGroup implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "group_id", updatable = false)
    private UUID groupId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "group_set_id")
    private UUID groupSetId;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "group_name", nullable = false, length = 255)
    private String groupName;

    @Column(name = "group_description")
    private String groupDescription;

    @Column(name = "group_type", length = 50)
    private String groupType;

    @Column(name = "max_members")
    private Integer maxMembers;

    @Column(name = "current_members")
    private Integer currentMembers;

    @Column(name = "date_created_group", nullable = false)
    private LocalDate dateCreatedGroup;

    @Column(name = "date_start")
    private LocalDate dateStart;

    @Column(name = "date_end")
    private LocalDate dateEnd;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
