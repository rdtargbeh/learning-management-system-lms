package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LogLevel;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>system_log</b> (Audit & Logging domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "system_log", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class SystemLog implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id", updatable = false)
    private UUID logId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_level", length = 20)
    private LogLevel logLevel;

    @Column(name = "log_category", length = 100)
    private String logCategory;

    @Column(name = "log_message")
    private String logMessage;

    @Column(name = "error_stack_trace")
    private String errorStackTrace;

    @Column(name = "affected_user_id")
    private UUID affectedUserId;

    @Column(name = "affected_entity_type", length = 100)
    private String affectedEntityType;

    @Column(name = "affected_entity_id")
    private UUID affectedEntityId;

    @Column(name = "source_system", length = 100)
    private String sourceSystem;

    @Column(name = "resolution_status", length = 50)
    private String resolutionStatus;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
