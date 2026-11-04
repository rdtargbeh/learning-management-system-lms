package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * Table <b>ebook_search</b> (E-Book Platform domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "ebook_search", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class EbookSearch implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "search_id", updatable = false)
    private UUID searchId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "ebook_id", nullable = false)
    private UUID ebookId;

    @Column(name = "chapter_id")
    private UUID chapterId;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "searchable_text")
    private String searchableText;

    @Column(name = "date_indexed", nullable = false)
    private OffsetDateTime dateIndexed;
}
