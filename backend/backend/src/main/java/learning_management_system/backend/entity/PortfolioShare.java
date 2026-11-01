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
import org.hibernate.annotations.Generated;

/**
 * Table <b>portfolio_share</b> (ePortfolio & Catalog domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "portfolio_share", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class PortfolioShare implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "portfolio_share_id", updatable = false)
    private UUID portfolioShareId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(name = "shared_with_user_id")
    private UUID sharedWithUserId;

    @Column(name = "shared_with_email", columnDefinition = "citext")
    private String sharedWithEmail;

    @Column(name = "access_token_hash", length = 255)
    private String accessTokenHash;

    @Column(name = "can_comment", nullable = false)
    private Boolean canComment;

    @Column(name = "date_expires")
    private OffsetDateTime dateExpires;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
