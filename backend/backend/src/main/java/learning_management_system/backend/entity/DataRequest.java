package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.RequestStatus;
import learning_management_system.backend.enums.RequestType;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>data_request</b> (Privacy & Records (FERPA / GDPR) domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "data_request", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class DataRequest implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "data_request_id", updatable = false)
    private UUID dataRequestId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "subject_user_id", nullable = false)
    private UUID subjectUserId;

    @Column(name = "requested_by")
    private UUID requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 30)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false, length = 20)
    private RequestStatus requestStatus;

    @Column(name = "legal_basis", length = 100)
    private String legalBasis;

    @Column(name = "request_details")
    private String requestDetails;

    @Column(name = "export_file_id")
    private UUID exportFileId;

    @Column(name = "date_due")
    private LocalDate dateDue;

    @Column(name = "date_completed")
    private OffsetDateTime dateCompleted;

    @Column(name = "handled_by")
    private UUID handledBy;

    @Column(name = "resolution_notes")
    private String resolutionNotes;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @Generated(event = {org.hibernate.generator.EventType.INSERT, org.hibernate.generator.EventType.UPDATE})
    @Column(name = "date_updated", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateUpdated;
}
