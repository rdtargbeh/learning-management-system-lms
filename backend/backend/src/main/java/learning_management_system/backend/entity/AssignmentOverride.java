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
 * Table <b>assignment_override</b> (Assignments & Coursework domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "assignment_override", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AssignmentOverride implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "override_id", updatable = false)
    private UUID overrideId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "assignment_id")
    private UUID assignmentId;

    @Column(name = "assessment_id")
    private UUID assessmentId;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "target_user_id")
    private UUID targetUserId;

    @Column(name = "target_section_id")
    private UUID targetSectionId;

    @Column(name = "target_group_id")
    private UUID targetGroupId;

    @Column(name = "target_tag_id")
    private UUID targetTagId;

    @Column(name = "discussion_checkpoint_id")
    private UUID discussionCheckpointId;

    @Column(name = "date_available")
    private OffsetDateTime dateAvailable;

    @Column(name = "date_due")
    private OffsetDateTime dateDue;

    @Column(name = "date_close")
    private OffsetDateTime dateClose;

    @Column(name = "max_attempts_override")
    private Integer maxAttemptsOverride;

    @Column(name = "time_limit_minutes_override")
    private Integer timeLimitMinutesOverride;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_by")
    private UUID createdBy;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
