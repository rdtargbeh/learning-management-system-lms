package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.MetricType;
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
 * Table <b>accreditation_metric</b> (Learning Standards & Compliance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "accreditation_metric", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AccreditationMetric implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "metric_id", updatable = false)
    private UUID metricId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "metric_name", nullable = false, length = 255)
    private String metricName;

    @Column(name = "metric_description")
    private String metricDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 50)
    private MetricType metricType;

    @Column(name = "accreditation_body", length = 255)
    private String accreditationBody;

    @Column(name = "metric_code", length = 100)
    private String metricCode;

    @Column(name = "target_value", precision = 10, scale = 2)
    private BigDecimal targetValue;

    @Column(name = "current_value", precision = 10, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "measurement_unit", length = 50)
    private String measurementUnit;

    @Column(name = "data_source", length = 100)
    private String dataSource;

    @Column(name = "collection_method", length = 100)
    private String collectionMethod;

    @Column(name = "collection_frequency", length = 50)
    private String collectionFrequency;

    @Column(name = "last_measured")
    private LocalDate lastMeasured;

    @Column(name = "next_measurement")
    private LocalDate nextMeasurement;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "measurement_data")
    private JsonNode measurementData;

    @Column(name = "evidence_location", length = 500)
    private String evidenceLocation;

    @Column(name = "is_met")
    private Boolean isMet;

    @Column(name = "variance_percent", precision = 8, scale = 2)
    private BigDecimal variancePercent;

    @Column(name = "improvement_plan")
    private String improvementPlan;

    @Column(name = "responsible_department", length = 255)
    private String responsibleDepartment;

    @Column(name = "responsible_user_id")
    private UUID responsibleUserId;

    @Column(name = "benchmark_value", precision = 10, scale = 2)
    private BigDecimal benchmarkValue;

    @Column(name = "peer_value", precision = 10, scale = 2)
    private BigDecimal peerValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "trending")
    private JsonNode trending;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
