package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ModuleType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>course_module</b> (Academic Structure domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "course_module", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class CourseModule implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "module_id", updatable = false)
    private UUID moduleId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "module_number", nullable = false)
    private Integer moduleNumber;

    @Column(name = "module_title", nullable = false, length = 255)
    private String moduleTitle;

    @Column(name = "module_description")
    private String moduleDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "module_type", length = 50)
    private ModuleType moduleType;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "date_start")
    private LocalDate dateStart;

    @Column(name = "date_end")
    private LocalDate dateEnd;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Column(name = "is_locked")
    private Boolean isLocked;

    @Column(name = "require_sequential_progress", nullable = false)
    private Boolean requireSequentialProgress;

    @Column(name = "date_unlock")
    private OffsetDateTime dateUnlock;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
