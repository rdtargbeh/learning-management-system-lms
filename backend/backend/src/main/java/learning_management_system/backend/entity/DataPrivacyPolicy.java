package learning_management_system.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.PolicyType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Table <b>data_privacy_policy</b> (Learning Standards & Compliance domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "data_privacy_policy", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class DataPrivacyPolicy implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "policy_id", updatable = false)
    private UUID policyId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "policy_name", nullable = false, length = 255)
    private String policyName;

    @Column(name = "policy_version", length = 50)
    private String policyVersion;

    @Column(name = "policy_description")
    private String policyDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 50)
    private PolicyType policyType;

    @Column(name = "jurisdiction", length = 100)
    private String jurisdiction;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_laws")
    private JsonNode applicableLaws;

    @Column(name = "data_collection_practices")
    private String dataCollectionPractices;

    @Column(name = "data_retention_policy")
    private String dataRetentionPolicy;

    @Column(name = "data_retention_period_days")
    private Integer dataRetentionPeriodDays;

    @Column(name = "third_party_sharing_policy")
    private String thirdPartySharingPolicy;

    @Column(name = "student_rights_policy")
    private String studentRightsPolicy;

    @Column(name = "parental_access_policy")
    private String parentalAccessPolicy;

    @Column(name = "data_breach_procedure")
    private String dataBreachProcedure;

    @Column(name = "encryption_standard", length = 100)
    private String encryptionStandard;

    @Column(name = "access_logging_enabled")
    private Boolean accessLoggingEnabled;

    @Column(name = "audit_logging_enabled")
    private Boolean auditLoggingEnabled;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_security_measures")
    private JsonNode dataSecurityMeasures;

    @Column(name = "is_published")
    private Boolean isPublished;

    @Column(name = "approved_date")
    private OffsetDateTime approvedDate;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "review_frequency", length = 50)
    private String reviewFrequency;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "policy_url", length = 500)
    private String policyUrl;

    @Column(name = "privacy_officer_id")
    private UUID privacyOfficerId;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
