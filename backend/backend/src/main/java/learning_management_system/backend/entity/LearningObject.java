package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LearningObjectDifficultyLevel;
import learning_management_system.backend.enums.LearningObjectType;
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
 * Table <b>learning_object</b> (Knowledge Base & Institutional Content domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "learning_object", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class LearningObject implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "object_id", updatable = false)
    private UUID objectId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(name = "object_name", nullable = false, length = 255)
    private String objectName;

    @Column(name = "object_description")
    private String objectDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false, length = 50)
    private LearningObjectType objectType;

    @Column(name = "subject_area", length = 100)
    private String subjectArea;

    @Column(name = "grade_level", length = 50)
    private String gradeLevel;

    @Column(name = "learning_outcome")
    private String learningOutcome;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 50)
    private LearningObjectDifficultyLevel difficultyLevel;

    @Column(name = "content_url", length = 500)
    private String contentUrl;

    @Column(name = "content_file_path", length = 500)
    private String contentFilePath;

    @Column(name = "file_size_bytes")
    private Integer fileSizeBytes;

    @Column(name = "file_format", length = 50)
    private String fileFormat;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "language_code", length = 10)
    private String languageCode;

    @Column(name = "is_accessible")
    private Boolean isAccessible;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "accessibility_features")
    private JsonNode accessibilityFeatures;

    @Column(name = "creator_id")
    private UUID creatorId;

    @Column(name = "creator_name", length = 255)
    private String creatorName;

    @Column(name = "creator_organization", length = 255)
    private String creatorOrganization;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @Column(name = "license_type", length = 100)
    private String licenseType;

    @Column(name = "license_url", length = 500)
    private String licenseUrl;

    @Column(name = "attribution_required")
    private Boolean attributionRequired;

    @Column(name = "usage_count")
    private Integer usageCount;

    @Column(name = "download_count")
    private Integer downloadCount;

    @Column(name = "rating_average", precision = 3, scale = 1)
    private BigDecimal ratingAverage;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "approval_date")
    private OffsetDateTime approvalDate;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
