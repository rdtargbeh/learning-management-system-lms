package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.UsageRight;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>file_storage</b> (File Management & Storage domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "file_storage", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class FileStorage implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "file_id", updatable = false)
    private UUID fileId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "folder_id")
    private UUID folderId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Column(name = "storage_provider", length = 50)
    private String storageProvider;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "is_virus_scanned")
    private Boolean isVirusScanned;

    @Column(name = "is_safe")
    private Boolean isSafe;

    @Column(name = "uploaded_by")
    private UUID uploadedBy;

    @Column(name = "access_count")
    private Integer accessCount;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_right", length = 30)
    private UsageRight usageRight;

    @Column(name = "license_code", length = 50)
    private String licenseCode;

    @Column(name = "copyright_holder", length = 255)
    private String copyrightHolder;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden;

    @Column(name = "date_unlock")
    private OffsetDateTime dateUnlock;

    @Column(name = "date_lock")
    private OffsetDateTime dateLock;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
