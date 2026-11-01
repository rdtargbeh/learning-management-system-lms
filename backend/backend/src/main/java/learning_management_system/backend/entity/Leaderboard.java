package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LeaderboardType;
import learning_management_system.backend.enums.RankingMetric;
import learning_management_system.backend.enums.TimePeriod;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>leaderboard</b> (Gamification & Achievements domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "leaderboard", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Leaderboard implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "leaderboard_id", updatable = false)
    private UUID leaderboardId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "leaderboard_name", nullable = false, length = 255)
    private String leaderboardName;

    @Column(name = "leaderboard_description")
    private String leaderboardDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "leaderboard_type", nullable = false, length = 50)
    private LeaderboardType leaderboardType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ranking_metric", nullable = false, length = 50)
    private RankingMetric rankingMetric;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_period", length = 50)
    private TimePeriod timePeriod;

    @Column(name = "display_limit")
    private Integer displayLimit;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous;

    @Column(name = "include_top_performers")
    private Boolean includeTopPerformers;

    @Column(name = "include_peer_group")
    private Boolean includePeerGroup;

    @Column(name = "peer_group_size")
    private Integer peerGroupSize;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "allow_ties")
    private Boolean allowTies;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
