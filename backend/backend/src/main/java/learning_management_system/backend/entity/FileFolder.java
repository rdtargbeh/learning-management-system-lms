package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.FileFolderContextType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>file_folder</b> (File Management & Storage domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "file_folder", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class FileFolder implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "folder_id", updatable = false)
    private UUID folderId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "parent_folder_id")
    private UUID parentFolderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "context_type", nullable = false, length = 20)
    private FileFolderContextType contextType;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "folder_name", nullable = false, length = 255)
    private String folderName;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Column(name = "date_unlock")
    private OffsetDateTime dateUnlock;

    @Column(name = "date_lock")
    private OffsetDateTime dateLock;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
