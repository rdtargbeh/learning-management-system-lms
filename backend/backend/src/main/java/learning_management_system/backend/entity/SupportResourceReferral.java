package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ReferralStatus;
import learning_management_system.backend.enums.ReferralUrgency;
import learning_management_system.backend.enums.SupportResourceReferralResourceType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>support_resource_referral</b> (Academic Advising & Student Support domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "support_resource_referral", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class SupportResourceReferral implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "referral_id", updatable = false)
    private UUID referralId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "advisor_id", nullable = false)
    private UUID advisorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 50)
    private SupportResourceReferralResourceType resourceType;

    @Column(name = "resource_name", length = 255)
    private String resourceName;

    @Column(name = "resource_description")
    private String resourceDescription;

    @Column(name = "resource_contact", length = 255)
    private String resourceContact;

    @Column(name = "resource_url", length = 500)
    private String resourceUrl;

    @Column(name = "referral_reason")
    private String referralReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "referral_urgency", length = 50)
    private ReferralUrgency referralUrgency;

    @Enumerated(EnumType.STRING)
    @Column(name = "referral_status", nullable = false, length = 50)
    private ReferralStatus referralStatus;

    @Column(name = "student_contacted_resource")
    private Boolean studentContactedResource;

    @Column(name = "date_contacted")
    private OffsetDateTime dateContacted;

    @Column(name = "resource_feedback")
    private String resourceFeedback;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "referral_notes")
    private String referralNotes;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
