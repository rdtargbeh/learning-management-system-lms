package learning_management_system.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LinkStatus;
import learning_management_system.backend.enums.ObserverStudentLinkAccessLevel;
import learning_management_system.backend.enums.Relationship;
import learning_management_system.backend.security.TenantEntityListener;
import learning_management_system.backend.security.TenantScoped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;

/**
 * Table <b>observer_student_link</b> (Identity, Accounts & Profiles domain).
 * Generated from the PostgreSQL catalog of the final schema (scripts/gen_entities.py); do not edit by hand.
 * Columns maintained by the database (date_created, date_updated, tenant_id defaults, triggers) are read back.
 */
@Entity
@Table(name = "observer_student_link", schema = "lms_core")
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantEntityListener.class)
public class ObserverStudentLink implements TenantScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "observer_link_id", updatable = false)
    private UUID observerLinkId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "observer_user_id", nullable = false)
    private UUID observerUserId;

    @Column(name = "student_user_id", nullable = false)
    private UUID studentUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false, length = 30)
    private Relationship relationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false, length = 20)
    private ObserverStudentLinkAccessLevel accessLevel;

    @Column(name = "consent_record_id")
    private UUID consentRecordId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_status", nullable = false, length = 20)
    private LinkStatus linkStatus;

    @Column(name = "date_linked", nullable = false)
    private OffsetDateTime dateLinked;

    @Column(name = "date_revoked")
    private OffsetDateTime dateRevoked;

    @Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dateCreated;
}
