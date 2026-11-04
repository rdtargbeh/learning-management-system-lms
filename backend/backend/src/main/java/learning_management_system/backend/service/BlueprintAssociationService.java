package learning_management_system.backend.service;

import learning_management_system.backend.dto.AssociateBlueprintCoursesRequest;
import learning_management_system.backend.dto.BlueprintAssociationDto;
import learning_management_system.backend.dto.BlueprintStatusDto;

import java.util.List;
import java.util.UUID;

/**
 * blueprint_association: which courses receive a blueprint course's content (Canvas blueprint courses). Course admins
 * (COURSE_MANAGE) associate and remove courses; a course has at most one blueprint at a time.
 */
public interface BlueprintAssociationService {

    /** The blueprint's associated courses (blueprint teachers and course admins). */
    List<BlueprintAssociationDto> list(UUID blueprintCourseId);

    /** Associates STANDARD courses that have no other blueprint. Re-associating a removed course re-activates it. */
    List<BlueprintAssociationDto> associate(UUID blueprintCourseId, AssociateBlueprintCoursesRequest r);

    /** Ends the association: the copies stay in the course and are no longer locked or synced. */
    void remove(UUID blueprintCourseId, UUID associationId);

    /** For an associated course's staff: its blueprint and locked items (404 when it has no blueprint). */
    BlueprintStatusDto status(UUID courseId);
}
