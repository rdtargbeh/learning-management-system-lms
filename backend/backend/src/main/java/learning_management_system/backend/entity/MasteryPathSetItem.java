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
 * Table <b>mastery_path_set_item</b> (Learning Content Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "mastery_path_set_item", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class MasteryPathSetItem implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "set_item_id", updatable = false)
    private UUID setItemId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "range_id", nullable = false)
    private UUID rangeId;

    @Column(name = "set_number", nullable = false)
    private Integer setNumber;

    @Column(name = "assignment_id")
    private UUID assignmentId;

    @Column(name = "assessment_id")
    private UUID assessmentId;

    @Column(name = "discussion_id")
    private UUID discussionId;

    @Column(name = "content_id")
    private UUID contentId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}
