package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Different dates for one student, section or group (table assignment_override). */
public record AssignmentOverrideDto(UUID overrideId, UUID assignmentId, String targetType, UUID targetUserId, UUID targetSectionId,
                                    UUID targetGroupId, OffsetDateTime dateAvailable, OffsetDateTime dateDue,
                                    OffsetDateTime dateClose, Integer maxAttemptsOverride, String reason) {}
