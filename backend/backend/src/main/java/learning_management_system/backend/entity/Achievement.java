package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AchievementLevel;
import learning_management_system.backend.enums.AchievementType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>achievement</b> (Gamification & Achievements domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "achievement", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Achievement implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "achievement_id", updatable = false)
    private UUID achievementId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "achievement_name", nullable = false, length = 255)
    private String achievementName;

    @Column(name = "achievement_description")
    private String achievementDescription;

    @Column(name = "achievement_icon_url", length = 500)
    private String achievementIconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_type", nullable = false, length = 50)
    private AchievementType achievementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_level", length = 50)
    private AchievementLevel achievementLevel;

    @Column(name = "points_reward")
    private Integer pointsReward;

    @Column(name = "xp_reward")
    private Integer xpReward;

    @Column(name = "unlock_criteria")
    private String unlockCriteria;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unlock_badge_ids")
    private JsonNode unlockBadgeIds;

    @Column(name = "is_hidden")
    private Boolean isHidden;

    @Column(name = "is_unlocked_by_default")
    private Boolean isUnlockedByDefault;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
