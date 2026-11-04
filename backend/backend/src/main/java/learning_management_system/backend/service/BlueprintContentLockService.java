package learning_management_system.backend.service;

import learning_management_system.backend.dto.BlueprintContentLockDto;
import learning_management_system.backend.dto.SetBlueprintLockRequest;
import learning_management_system.backend.enums.BlueprintContentLockObjectType;
import learning_management_system.backend.enums.BlueprintLockAspect;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * blueprint_content_lock: which parts of a blueprint item associated courses may not change. The blueprint course's
 * teachers (or COURSE_MANAGE) set locks; the content, assignment, module and course services ask
 * {@link #requireEditable} before changing an item in an associated course.
 */
public interface BlueprintContentLockService {

    List<BlueprintContentLockDto> list(UUID blueprintCourseId);

    /** Creates or replaces the lock of one item; every flag off removes it (returns null). */
    BlueprintContentLockDto set(UUID blueprintCourseId, SetBlueprintLockRequest r);

    void remove(UUID blueprintCourseId, UUID lockId);

    /** Locks that apply to an associated course's items (with the course's copy ids). */
    List<BlueprintContentLockDto> locksInCourse(UUID courseId);

    /**
     * 409 BLUEPRINT_LOCKED when the item is a blueprint copy whose lock covers one of the aspects. Items of courses
     * without a blueprint, and items the course created itself, are never locked. itemId is ignored for SETTINGS.
     */
    void requireEditable(UUID courseId, BlueprintContentLockObjectType type, UUID itemId, Set<BlueprintLockAspect> aspects);
}
