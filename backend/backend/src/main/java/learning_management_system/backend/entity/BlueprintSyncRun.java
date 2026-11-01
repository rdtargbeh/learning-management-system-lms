package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.BlueprintSyncRunStatus;
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
 * Table <b>blueprint_sync_run</b> (Academic Structure domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "blueprint_sync_run", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class BlueprintSyncRun implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sync_run_id", updatable = false)
    private UUID syncRunId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "blueprint_course_id", nullable = false)
    private UUID blueprintCourseId;

    @Column(name = "initiated_by")
    private UUID initiatedBy;

    @Column(name = "sync_comment")
    private String syncComment;

    @Column(name = "include_course_settings", nullable = false)
    private Boolean includeCourseSettings;

    @Column(name = "notify_users", nullable = false)
    private Boolean notifyUsers;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_status", nullable = false, length = 20)
    private BlueprintSyncRunStatus runStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "changes")
    private JsonNode changes;

    @Column(name = "courses_synced", nullable = false)
    private Integer coursesSynced;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "exceptions")
    private JsonNode exceptions;

    @Column(name = "date_start")
    private OffsetDateTime dateStart;

    @Column(name = "date_end")
    private OffsetDateTime dateEnd;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
