package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AccessibilityScanStatus;
import learning_management_system.backend.enums.AccessibilityScanWcagLevel;
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
 * Table <b>accessibility_scan</b> (File Management & Storage domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "accessibility_scan", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AccessibilityScan implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "scan_id", updatable = false)
    private UUID scanId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "entity_type", nullable = false, length = 30)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "file_id")
    private UUID fileId;

    @Column(name = "accessibility_score", precision = 5, scale = 2)
    private BigDecimal accessibilityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "wcag_level", length = 5)
    private AccessibilityScanWcagLevel wcagLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "issues")
    private JsonNode issues;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_status", nullable = false, length = 20)
    private AccessibilityScanStatus scanStatus;

    @Column(name = "date_scanned")
    private OffsetDateTime dateScanned;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
