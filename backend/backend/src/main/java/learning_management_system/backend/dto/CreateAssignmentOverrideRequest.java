package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Give one student (STUDENT), section (SECTION) or group (GROUP) their own dates or extra attempts. When several
 * overrides apply to a student, the latest dates and the most attempts win.
 */
public record CreateAssignmentOverrideRequest(@NotNull @Pattern(regexp = "STUDENT|SECTION|GROUP") String targetType, UUID targetUserId,
                                              UUID targetSectionId, UUID targetGroupId, OffsetDateTime dateAvailable,
                                              OffsetDateTime dateDue, OffsetDateTime dateClose, @Positive Integer maxAttemptsOverride,
                                              String reason) {}
