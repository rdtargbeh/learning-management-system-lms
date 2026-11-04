package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>catalog_listing</b> (ePortfolio & Catalog domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "catalog_listing", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class CatalogListing implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "listing_id", updatable = false)
    private UUID listingId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "listing_title", nullable = false, length = 255)
    private String listingTitle;

    @Column(name = "listing_slug", nullable = false, length = 255)
    private String listingSlug;

    @Column(name = "summary")
    private String summary;

    @Column(name = "listing_image_url", length = 1000)
    private String listingImageUrl;

    @Column(name = "price_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAmount;

    @Column(name = "currency", nullable = false, length = 3, columnDefinition = "bpchar")
    private String currency;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible;

    @Column(name = "enrollment_open", nullable = false)
    private Boolean enrollmentOpen;

    @Column(name = "date_enroll_start")
    private OffsetDateTime dateEnrollStart;

    @Column(name = "date_enroll_end")
    private OffsetDateTime dateEnrollEnd;

    @Column(name = "awards_badge_id")
    private UUID awardsBadgeId;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
