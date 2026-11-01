package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AccessStatus;

/** A support-access request/session (table platform_support_access). */
public record PlatformSupportAccessDto(UUID supportAccessId, UUID tenantId, UUID platformUserId, UUID impersonatedUserId,
                                       String accessReason, String ticketReference, UUID approvedBy,
                                       AccessStatus accessStatus, OffsetDateTime dateStart,
                                       OffsetDateTime dateExpires, OffsetDateTime dateEnded) {}
