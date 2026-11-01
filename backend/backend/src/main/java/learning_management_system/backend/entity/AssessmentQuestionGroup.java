package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
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
 * Table <b>assessment_question_group</b> (Assessment & Testing domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "assessment_question_group", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AssessmentQuestionGroup implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "question_group_id", updatable = false)
    private UUID questionGroupId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @Column(name = "bank_id")
    private UUID bankId;

    @Column(name = "group_name", length = 255)
    private String groupName;

    @Column(name = "pick_count", nullable = false)
    private Integer pickCount;

    @Column(name = "points_per_question", nullable = false, precision = 8, scale = 2)
    private BigDecimal pointsPerQuestion;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
