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
 * Table <b>user_accessibility_settings</b> (Identity, Accounts & Profiles domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "user_accessibility_settings", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class UserAccessibilitySettings implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "accessibility_id", updatable = false)
    private UUID accessibilityId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "high_contrast_enabled")
    private Boolean highContrastEnabled;

    @Column(name = "text_size_percentage")
    private Integer textSizePercentage;

    @Column(name = "screen_reader_enabled")
    private Boolean screenReaderEnabled;

    @Column(name = "dyslexia_friendly_font")
    private Boolean dyslexiaFriendlyFont;

    @Column(name = "font_family_accessible", length = 100)
    private String fontFamilyAccessible;

    @Column(name = "captions_enabled")
    private Boolean captionsEnabled;

    @Column(name = "transcripts_enabled")
    private Boolean transcriptsEnabled;

    @Column(name = "extended_time_on_assessments")
    private Boolean extendedTimeOnAssessments;

    @Column(name = "extended_time_percentage")
    private Integer extendedTimePercentage;

    @Column(name = "reduced_motion_enabled")
    private Boolean reducedMotionEnabled;

    @Column(name = "color_blind_mode")
    private Boolean colorBlindMode;

    @Column(name = "color_blind_type", length = 50)
    private String colorBlindType;

    @Column(name = "keyboard_navigation_enabled")
    private Boolean keyboardNavigationEnabled;

    @Column(name = "disable_animations")
    private Boolean disableAnimations;

    @Column(name = "low_data_mode")
    private Boolean lowDataMode;

    @Column(name = "accessibility_notes")
    private String accessibilityNotes;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
