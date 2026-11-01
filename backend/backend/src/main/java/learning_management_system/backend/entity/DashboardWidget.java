package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.WidgetType;
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
 * Table <b>dashboard_widget</b> (Learning Analytics & Reporting domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "dashboard_widget", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class DashboardWidget implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "widget_id", updatable = false)
    private UUID widgetId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "dashboard_id", length = 100)
    private String dashboardId;

    @Column(name = "widget_title", nullable = false, length = 255)
    private String widgetTitle;

    @Column(name = "widget_description")
    private String widgetDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "widget_type", nullable = false, length = 50)
    private WidgetType widgetType;

    @Column(name = "data_source", length = 100)
    private String dataSource;

    @Column(name = "metric_key", length = 100)
    private String metricKey;

    @Column(name = "visualization_type", length = 50)
    private String visualizationType;

    @Column(name = "position_row")
    private Integer positionRow;

    @Column(name = "position_column")
    private Integer positionColumn;

    @Column(name = "width_units")
    private Integer widthUnits;

    @Column(name = "height_units")
    private Integer heightUnits;

    @Column(name = "refresh_interval_minutes")
    private Integer refreshIntervalMinutes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filters")
    private JsonNode filters;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "display_settings")
    private JsonNode displaySettings;

    @Column(name = "data_aggregation", length = 50)
    private String dataAggregation;

    @Column(name = "time_period", length = 50)
    private String timePeriod;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "last_refreshed")
    private OffsetDateTime lastRefreshed;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
