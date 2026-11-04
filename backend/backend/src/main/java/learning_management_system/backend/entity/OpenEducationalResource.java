package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LicenseType;
import learning_management_system.backend.enums.OerType;
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
 * Table <b>open_educational_resource</b> (Knowledge Base & Institutional Content domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "open_educational_resource", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class OpenEducationalResource implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "oer_id", updatable = false)
    private UUID oerId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "oer_title", nullable = false, length = 255)
    private String oerTitle;

    @Column(name = "oer_description")
    private String oerDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "oer_type", nullable = false, length = 50)
    private OerType oerType;

    @Column(name = "resource_url", length = 500)
    private String resourceUrl;

    @Column(name = "creator_name", length = 255)
    private String creatorName;

    @Column(name = "creator_organization", length = 255)
    private String creatorOrganization;

    @Column(name = "creator_contact", length = 255)
    private String creatorContact;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", nullable = false, length = 100)
    private LicenseType licenseType;

    @Column(name = "license_url", length = 500)
    private String licenseUrl;

    @Column(name = "is_derivative_ok")
    private Boolean isDerivativeOk;

    @Column(name = "is_commercial_use_ok")
    private Boolean isCommercialUseOk;

    @Column(name = "attribution_text")
    private String attributionText;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "language_code", length = 10)
    private String languageCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "subject_areas")
    private JsonNode subjectAreas;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "grade_levels")
    private JsonNode gradeLevels;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "learning_outcomes")
    private JsonNode learningOutcomes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "accessibility_features")
    private JsonNode accessibilityFeatures;

    @Column(name = "file_format", length = 50)
    private String fileFormat;

    @Column(name = "file_size_bytes")
    private Integer fileSizeBytes;

    @Column(name = "total_downloads")
    private Integer totalDownloads;

    @Column(name = "average_rating", precision = 3, scale = 1)
    private BigDecimal averageRating;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "approval_date")
    private OffsetDateTime approvalDate;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "last_accessed")
    private OffsetDateTime lastAccessed;

    @Column(name = "usage_count")
    private Integer usageCount;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
