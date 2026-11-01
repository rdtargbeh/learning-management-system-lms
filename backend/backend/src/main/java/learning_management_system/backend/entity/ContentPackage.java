package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ImportStatus;
import learning_management_system.backend.enums.PackageStatus;
import learning_management_system.backend.enums.PackageType;
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
 * Table <b>content_package</b> (Integrations & Interoperability domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "content_package", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ContentPackage implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "package_id", updatable = false)
    private UUID packageId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "package_name", nullable = false, length = 255)
    private String packageName;

    @Column(name = "package_description")
    private String packageDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false, length = 50)
    private PackageType packageType;

    @Column(name = "package_file_path", length = 500)
    private String packageFilePath;

    @Column(name = "package_file_size")
    private Integer packageFileSize;

    @Column(name = "package_format", length = 50)
    private String packageFormat;

    @Column(name = "package_version", length = 50)
    private String packageVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_status", nullable = false, length = 50)
    private PackageStatus packageStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "import_status", length = 50)
    private ImportStatus importStatus;

    @Column(name = "import_progress", precision = 5, scale = 2)
    private BigDecimal importProgress;

    @Column(name = "imported_items_count")
    private Integer importedItemsCount;

    @Column(name = "import_errors")
    private Integer importErrors;

    @Column(name = "import_warnings")
    private Integer importWarnings;

    @Column(name = "import_date")
    private OffsetDateTime importDate;

    @Column(name = "imported_by")
    private UUID importedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "manifest_data")
    private JsonNode manifestData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private JsonNode metadata;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
