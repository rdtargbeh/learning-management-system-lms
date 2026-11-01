package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.FormatType;
import learning_management_system.backend.enums.GenerationStatus;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>alternative_format</b> (File Management & Storage domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "alternative_format", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class AlternativeFormat implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "alternative_format_id", updatable = false)
    private UUID alternativeFormatId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "source_file_id", nullable = false)
    private UUID sourceFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "format_type", nullable = false, length = 30)
    private FormatType formatType;

    @Column(name = "output_file_id")
    private UUID outputFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_status", nullable = false, length = 20)
    private GenerationStatus generationStatus;

    @Column(name = "download_count", nullable = false)
    private Integer downloadCount;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
