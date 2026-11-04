package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.util.UUID;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Table <b>course_pace_item</b> (Learning Content Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "course_pace_item", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class CoursePaceItem implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pace_item_id", updatable = false)
    private UUID paceItemId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "pace_id", nullable = false)
    private UUID paceId;

    @Column(name = "module_id", nullable = false)
    private UUID moduleId;

    @Column(name = "content_id")
    private UUID contentId;

    @Column(name = "assignment_id")
    private UUID assignmentId;

    @Column(name = "assessment_id")
    private UUID assessmentId;

    @Column(name = "discussion_id")
    private UUID discussionId;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}
