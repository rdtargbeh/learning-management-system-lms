package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.RubricType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>rubric_template</b> (Grading & Evaluation domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "rubric_template", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class RubricTemplate implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "template_id", updatable = false)
    private UUID templateId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "org_unit_id")
    private UUID orgUnitId;

    @Column(name = "template_name", nullable = false, length = 255)
    private String templateName;

    @Column(name = "template_description")
    private String templateDescription;

    @Column(name = "creator_id")
    private UUID creatorId;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "is_shared")
    private Boolean isShared;

    @Column(name = "total_points", precision = 8, scale = 2)
    private BigDecimal totalPoints;

    @Column(name = "hide_points", nullable = false)
    private Boolean hidePoints;

    @Column(name = "free_form_comments", nullable = false)
    private Boolean freeFormComments;

    @Enumerated(EnumType.STRING)
    @Column(name = "rubric_type", length = 50)
    private RubricType rubricType;

    @Column(name = "is_active")
    private Boolean isActive;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
