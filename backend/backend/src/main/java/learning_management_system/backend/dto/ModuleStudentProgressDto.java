package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ProgressStatus;

/** One student's progress in a module, for the teacher's progress view. */
public record ModuleStudentProgressDto(UUID userId, String username, String displayName, ProgressStatus progressStatus,
                                       int requirementsMet, int requirementsTotal, OffsetDateTime dateCompleted) {}
