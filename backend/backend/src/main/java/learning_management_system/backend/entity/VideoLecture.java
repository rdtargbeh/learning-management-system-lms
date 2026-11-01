package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.VideoProvider;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>video_lecture</b> (Learning Content Management domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "video_lecture", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class VideoLecture implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "video_id", updatable = false)
    private UUID videoId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_provider", length = 50)
    private VideoProvider videoProvider;

    @Column(name = "video_duration_seconds")
    private Integer videoDurationSeconds;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "transcript")
    private String transcript;

    @Column(name = "transcript_url", length = 500)
    private String transcriptUrl;

    @Column(name = "captions_url", length = 500)
    private String captionsUrl;

    @Column(name = "captions_available")
    private Boolean captionsAvailable;

    @Column(name = "quality_available", length = 100)
    private String qualityAvailable;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
