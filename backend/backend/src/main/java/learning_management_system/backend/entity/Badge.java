package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.BadgeCategory;
import learning_management_system.backend.enums.BadgeRarity;
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
 * Table <b>badge</b> (Gamification & Achievements domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "badge", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Badge implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "badge_id", updatable = false)
    private UUID badgeId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "badge_name", nullable = false, length = 255)
    private String badgeName;

    @Column(name = "badge_description")
    private String badgeDescription;

    @Column(name = "badge_icon_url", length = 500)
    private String badgeIconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_category", nullable = false, length = 50)
    private BadgeCategory badgeCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_rarity", nullable = false, length = 50)
    private BadgeRarity badgeRarity;

    @Column(name = "points_value")
    private Integer pointsValue;

    @Column(name = "xp_value")
    private Integer xpValue;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_hidden")
    private Boolean isHidden;

    @Column(name = "is_special")
    private Boolean isSpecial;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "open_badge_class")
    private JsonNode openBadgeClass;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
