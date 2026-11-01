package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.EbookSourceType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>ebook</b> (E-Book Platform domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "ebook", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class Ebook implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ebook_id", updatable = false)
    private UUID ebookId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "ebook_title", nullable = false, length = 255)
    private String ebookTitle;

    @Column(name = "ebook_description")
    private String ebookDescription;

    @Column(name = "author", length = 255)
    private String author;

    @Column(name = "publisher", length = 255)
    private String publisher;

    @Column(name = "isbn", length = 20)
    private String isbn;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "language", length = 10)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 50)
    private EbookSourceType sourceType;

    @Column(name = "third_party_provider", length = 100)
    private String thirdPartyProvider;

    @Column(name = "third_party_id", length = 255)
    private String thirdPartyId;

    @Column(name = "third_party_access_url", length = 500)
    private String thirdPartyAccessUrl;

    @Column(name = "is_required_reading")
    private Boolean isRequiredReading;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "date_assigned")
    private LocalDate dateAssigned;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
