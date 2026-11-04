package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.CollectionFrequency;
import learning_management_system.backend.enums.DatasetType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>analytics_dataset</b> (Learning Analytics & Reporting domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "analytics_dataset", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AnalyticsDataset implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "dataset_id", updatable = false)
    private UUID datasetId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "dataset_name", nullable = false, length = 255)
    private String datasetName;

    @Column(name = "dataset_description")
    private String datasetDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "dataset_type", nullable = false, length = 50)
    private DatasetType datasetType;

    @Column(name = "data_source", length = 100)
    private String dataSource;

    @Column(name = "metric_type", length = 50)
    private String metricType;

    @Column(name = "aggregation_level", length = 50)
    private String aggregationLevel;

    @Column(name = "time_period", length = 50)
    private String timePeriod;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "total_users")
    private Integer totalUsers;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "last_collected")
    private OffsetDateTime lastCollected;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_frequency", length = 50)
    private CollectionFrequency collectionFrequency;

    @Column(name = "next_collection")
    private OffsetDateTime nextCollection;

    @Column(name = "retention_days")
    private Integer retentionDays;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
