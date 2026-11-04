package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ReportScope;
import learning_management_system.backend.enums.ReportStatus;
import learning_management_system.backend.enums.ReportType;
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
 * Table <b>custom_report</b> (Learning Analytics & Reporting domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "custom_report", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class CustomReport implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_id", updatable = false)
    private UUID reportId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "report_name", nullable = false, length = 255)
    private String reportName;

    @Column(name = "report_description")
    private String reportDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_scope", length = 50)
    private ReportScope reportScope;

    @Column(name = "scope_id")
    private UUID scopeId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", length = 50)
    private ReportStatus reportStatus;

    @Column(name = "data_range_start")
    private LocalDate dataRangeStart;

    @Column(name = "data_range_end")
    private LocalDate dataRangeEnd;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filters_applied")
    private JsonNode filtersApplied;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_parameters")
    private JsonNode reportParameters;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "visualization_settings")
    private JsonNode visualizationSettings;

    @Column(name = "generated_at")
    private OffsetDateTime generatedAt;

    @Column(name = "generation_time_seconds")
    private Integer generationTimeSeconds;

    @Column(name = "report_file_path", length = 500)
    private String reportFilePath;

    @Column(name = "report_file_size_bytes")
    private Integer reportFileSizeBytes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "distribution_list")
    private JsonNode distributionList;

    @Column(name = "last_distributed")
    private OffsetDateTime lastDistributed;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "last_viewed")
    private OffsetDateTime lastViewed;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
