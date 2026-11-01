package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ContentDensity;
import learning_management_system.backend.enums.LearningStyle;
import learning_management_system.backend.enums.ThemePreference;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>user_preferences</b> (Identity, Accounts & Profiles domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "user_preferences", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class UserPreferences implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "preference_id", updatable = false)
    private UUID preferenceId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_style", length = 50)
    private LearningStyle learningStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme_preference", length = 50)
    private ThemePreference themePreference;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_density", length = 50)
    private ContentDensity contentDensity;

    @Column(name = "show_instructor_tips")
    private Boolean showInstructorTips;

    @Column(name = "show_learning_analytics")
    private Boolean showLearningAnalytics;

    @Column(name = "newsletter_subscription")
    private Boolean newsletterSubscription;

    @Column(name = "marketing_communications")
    private Boolean marketingCommunications;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
