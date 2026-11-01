package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.UnitType;
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
 * Table <b>org_unit</b> (Academic Structure domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "org_unit", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class OrgUnit implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "org_unit_id", updatable = false)
    private UUID orgUnitId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "parent_org_unit_id")
    private UUID parentOrgUnitId;

    @Column(name = "unit_name", nullable = false, length = 255)
    private String unitName;

    @Column(name = "unit_code", length = 50)
    private String unitCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, length = 50)
    private UnitType unitType;

    @Column(name = "sis_source_id", length = 255)
    private String sisSourceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "branding")
    private JsonNode branding;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private JsonNode metadata;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
