package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** For an associated course: its blueprint, the last sync, and which of its items are locked. */
public record BlueprintStatusDto(UUID courseId, UUID blueprintCourseId, String blueprintCourseCode, String blueprintCourseTitle,
                                 OffsetDateTime dateLastSynced, List<BlueprintContentLockDto> locks) {}
