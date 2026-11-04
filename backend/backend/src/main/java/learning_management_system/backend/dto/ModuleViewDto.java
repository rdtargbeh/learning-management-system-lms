package learning_management_system.backend.dto;

import java.util.List;
import learning_management_system.backend.enums.ProgressStatus;

/**
 * A module with its items. For students, progressStatus is LOCKED, UNLOCKED, STARTED or COMPLETED and lockReason
 * says why a locked module is locked; for staff both are null.
 */
public record ModuleViewDto(CourseModuleDto module, ProgressStatus progressStatus, String lockReason, List<ModuleItemDto> items) {}
