package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.EbookAnnotationType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>ebook_annotation</b> (E-Book Platform domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "ebook_annotation", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class EbookAnnotation implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "annotation_id", updatable = false)
    private UUID annotationId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "chapter_id", nullable = false)
    private UUID chapterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "annotation_type", length = 50)
    private EbookAnnotationType annotationType;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "page_position_percent", precision = 5, scale = 2)
    private BigDecimal pagePositionPercent;

    @Column(name = "highlighted_text")
    private String highlightedText;

    @Column(name = "annotation_note")
    private String annotationNote;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "is_private")
    private Boolean isPrivate;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
