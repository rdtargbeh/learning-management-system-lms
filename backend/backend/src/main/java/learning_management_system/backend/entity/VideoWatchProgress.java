package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>video_watch_progress</b> (Content Delivery & Learning Activity domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "video_watch_progress", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class VideoWatchProgress implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "watch_progress_id", updatable = false)
    private UUID watchProgressId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "video_id", nullable = false)
    private UUID videoId;

    @Column(name = "total_duration_seconds")
    private Integer totalDurationSeconds;

    @Column(name = "current_position_seconds")
    private Integer currentPositionSeconds;

    @Column(name = "percent_watched", precision = 5, scale = 2)
    private BigDecimal percentWatched;

    @Column(name = "watch_count")
    private Integer watchCount;

    @Column(name = "total_watch_time_seconds")
    private Integer totalWatchTimeSeconds;

    @Column(name = "date_first_watched")
    private OffsetDateTime dateFirstWatched;

    @Column(name = "date_last_watched")
    private OffsetDateTime dateLastWatched;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @Column(name = "playback_speed", precision = 2, scale = 1)
    private BigDecimal playbackSpeed;

    @Column(name = "captions_enabled")
    private Boolean captionsEnabled;

    @Column(name = "is_seeking")
    private Boolean isSeeking;

    @Column(name = "quality_watched", length = 20)
    private String qualityWatched;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "has_paused_at")
    private JsonNode hasPausedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "has_rewound_at")
    private JsonNode hasRewoundAt;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
