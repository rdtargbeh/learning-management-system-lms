package learning_management_system.backend.service;

import learning_management_system.backend.dto.CreateReleaseConditionRequest;
import learning_management_system.backend.dto.ReleaseConditionDto;
import learning_management_system.backend.enums.TargetType;

import java.util.List;
import java.util.UUID;

/**
 * release_condition: conditional release (as in D2L/Brightspace). The course's teachers attach conditions to content,
 * modules and assignments; students cannot open the target until one condition group is fully met. Staff are never
 * held back.
 */
public interface ReleaseConditionService {

    /** The course's conditions, optionally for one target (course staff). */
    List<ReleaseConditionDto> list(UUID courseId, TargetType targetType, UUID targetId);

    ReleaseConditionDto create(UUID courseId, CreateReleaseConditionRequest r);

    void delete(UUID courseId, UUID conditionId);

    /** Internal: why the student cannot open the target yet, or null when it is released (or has no conditions). */
    String lockReason(UUID courseId, TargetType targetType, UUID targetId, UUID userId);
}
