package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.SisSyncRunStatus;
import learning_management_system.backend.enums.SyncType;
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
 * Table <b>sis_sync_run</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "sis_sync_run", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class SisSyncRun implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sync_run_id", updatable = false)
    private UUID syncRunId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "sis_integration_id", nullable = false)
    private UUID sisIntegrationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_type", nullable = false, length = 30)
    private SyncType syncType;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_status", nullable = false, length = 30)
    private SisSyncRunStatus runStatus;

    @Column(name = "source_file_id")
    private UUID sourceFileId;

    @Column(name = "is_diff_mode", nullable = false)
    private Boolean isDiffMode;

    @Column(name = "records_processed", nullable = false)
    private Integer recordsProcessed;

    @Column(name = "records_created", nullable = false)
    private Integer recordsCreated;

    @Column(name = "records_updated", nullable = false)
    private Integer recordsUpdated;

    @Column(name = "records_deleted", nullable = false)
    private Integer recordsDeleted;

    @Column(name = "records_failed", nullable = false)
    private Integer recordsFailed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error_report")
    private JsonNode errorReport;

    @Column(name = "date_start")
    private OffsetDateTime dateStart;

    @Column(name = "date_end")
    private OffsetDateTime dateEnd;

    @Column(name = "triggered_by")
    private UUID triggeredBy;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
