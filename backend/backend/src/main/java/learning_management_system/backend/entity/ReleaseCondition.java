package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ConditionType;
import learning_management_system.backend.enums.ReleaseConditionSourceType;
import learning_management_system.backend.enums.TargetType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>release_condition</b> (Learning Content Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "release_condition", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ReleaseCondition implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "condition_id", updatable = false)
    private UUID conditionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 20)
    private ConditionType conditionType;

    @Column(name = "date_from")
    private OffsetDateTime dateFrom;

    @Column(name = "date_until")
    private OffsetDateTime dateUntil;

    @Column(name = "member_user_id")
    private UUID memberUserId;

    @Column(name = "member_group_id")
    private UUID memberGroupId;

    @Column(name = "member_section_id")
    private UUID memberSectionId;

    @Column(name = "member_tag_id")
    private UUID memberTagId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20)
    private ReleaseConditionSourceType sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "min_score_percent", precision = 5, scale = 2)
    private BigDecimal minScorePercent;

    @Column(name = "max_score_percent", precision = 5, scale = 2)
    private BigDecimal maxScorePercent;

    @Column(name = "condition_group", nullable = false)
    private Integer conditionGroup;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
