package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.TemplateType;
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
 * Table <b>report_template</b> (Learning Analytics & Reporting domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "report_template", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ReportTemplate implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "template_id", updatable = false)
    private UUID templateId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "template_name", nullable = false, length = 255)
    private String templateName;

    @Column(name = "template_description")
    private String templateDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 50)
    private TemplateType templateType;

    @Column(name = "report_category", length = 50)
    private String reportCategory;

    @Column(name = "data_source", length = 100)
    private String dataSource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dimensions")
    private JsonNode dimensions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metrics")
    private JsonNode metrics;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filters")
    private JsonNode filters;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sort_order")
    private JsonNode sortOrder;

    @Column(name = "visualization_type", length = 50)
    private String visualizationType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout_config")
    private JsonNode layoutConfig;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "is_system_template")
    private Boolean isSystemTemplate;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "is_active")
    private Boolean isActive;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
