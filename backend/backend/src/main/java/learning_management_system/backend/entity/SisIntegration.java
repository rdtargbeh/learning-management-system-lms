package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.Protocol;
import learning_management_system.backend.enums.SisIntegrationProvider;
import learning_management_system.backend.enums.SyncDirection;
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
 * Table <b>sis_integration</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "sis_integration", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class SisIntegration implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sis_integration_id", updatable = false)
    private UUID sisIntegrationId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private SisIntegrationProvider provider;

    @Column(name = "integration_name", nullable = false, length = 255)
    private String integrationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol", nullable = false, length = 30)
    private Protocol protocol;

    @Column(name = "base_url", length = 1000)
    private String baseUrl;

    @Column(name = "credentials_secret_ref", length = 255)
    private String credentialsSecretRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_direction", nullable = false, length = 20)
    private SyncDirection syncDirection;

    @Column(name = "sync_schedule", length = 100)
    private String syncSchedule;

    @Column(name = "grade_passback_enabled", nullable = false)
    private Boolean gradePassbackEnabled;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config")
    private JsonNode config;

    @Column(name = "date_last_sync")
    private OffsetDateTime dateLastSync;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
