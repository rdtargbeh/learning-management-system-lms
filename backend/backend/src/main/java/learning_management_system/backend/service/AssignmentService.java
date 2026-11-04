package learning_management_system.backend.service;

import learning_management_system.backend.dto.AssignmentDto;
import learning_management_system.backend.dto.AssignmentOverrideDto;
import learning_management_system.backend.dto.AssignmentViewDto;
import learning_management_system.backend.dto.CreateAssignmentOverrideRequest;
import learning_management_system.backend.dto.CreateAssignmentRequest;
import learning_management_system.backend.dto.MoveContentRequest;
import learning_management_system.backend.dto.UpdateAssignmentRequest;
import learning_management_system.backend.entity.Assignment;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Assignments (Canvas assignments / Blackboard assessments of type assignment). Teachers create, publish, place in
 * modules and give students, sections or groups their own dates (overrides). Students see published assignments with
 * their own dates, attempts and posted grade.
 */
public interface AssignmentService {

    /** A student's dates and attempt limit after overrides. Dates are moments (due = end of the due day in the course time zone). */
    record Schedule(OffsetDateTime available, OffsetDateTime due, OffsetDateTime close, Integer maxAttempts) {}

    List<AssignmentViewDto> list(UUID courseId);

    AssignmentViewDto get(UUID assignmentId);

    AssignmentDto create(UUID courseId, CreateAssignmentRequest r);

    AssignmentDto update(UUID assignmentId, UpdateAssignmentRequest r);

    /** Unpublishing is refused once students have submitted. */
    AssignmentDto setPublished(UUID assignmentId, boolean published);

    AssignmentDto move(UUID assignmentId, MoveContentRequest r);

    /** Deleting is refused once students have submitted. */
    void delete(UUID assignmentId);

    List<AssignmentOverrideDto> overrides(UUID assignmentId);

    AssignmentOverrideDto addOverride(UUID assignmentId, CreateAssignmentOverrideRequest r);

    void removeOverride(UUID assignmentId, UUID overrideId);

    /** The student's own dates and attempt limit. */
    Schedule schedule(Assignment assignment, UUID userId);

    /** Why the student cannot hand in a new attempt now, or null when they can. */
    String submitLockReason(Assignment assignment, UUID userId);

    /** Attempts that count toward the limit: handed in and not reassigned. */
    int attemptsUsed(UUID assignmentId, UUID userId);
}
