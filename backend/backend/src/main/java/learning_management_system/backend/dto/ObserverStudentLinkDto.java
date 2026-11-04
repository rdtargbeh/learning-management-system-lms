package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LinkStatus;
import learning_management_system.backend.enums.ObserverStudentLinkAccessLevel;
import learning_management_system.backend.enums.Relationship;

/** An observer-to-student link (table observer_student_link). */
public record ObserverStudentLinkDto(UUID observerLinkId, UUID observerUserId, UUID studentUserId,
                                     Relationship relationship, ObserverStudentLinkAccessLevel accessLevel,
                                     LinkStatus linkStatus, UUID consentRecordId, OffsetDateTime dateLinked,
                                     OffsetDateTime dateRevoked) {}
