package learning_management_system.backend.service;

import learning_management_system.backend.dto.ModuleStudentProgressDto;
import learning_management_system.backend.entity.Content;
import learning_management_system.backend.entity.StudentContentProgress;
import learning_management_system.backend.enums.ProgressStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Works out, for one student, which modules and items are open, what they have completed, and keeps
 * student_module_progress up to date (Canvas module progression).
 * <ul>
 *   <li>A module is LOCKED while the teacher has locked it, its unlock date is in the future, or a prerequisite module
 *       is not completed; then UNLOCKED, STARTED (something done) or COMPLETED (every requirement met).</li>
 *   <li>A module without requirements counts as COMPLETED once unlocked, so it never blocks the next one.</li>
 *   <li>With sequential progress, every item after the first unmet requirement stays locked.</li>
 *   <li>Unpublished modules and items are invisible to students and are ignored.</li>
 * </ul>
 * Callers authorize; only students (active STUDENT/AUDIT/VISITING enrollment) have progress recorded.
 */
public interface ModuleProgressService {

    /** The state of one module for one student. */
    record ModuleState(UUID moduleId, ProgressStatus status, String lockReason, Set<UUID> metRequirementIds,
                       Set<UUID> completedContentIds, Map<UUID, String> lockedContent) {}

    /** Evaluates every module of the course the student can see (in course order) and saves changed progress rows. */
    Map<UUID, ModuleState> evaluate(UUID courseId, UUID userId);

    /** True when the user takes a seat in the course (their views and completions are tracked). */
    boolean isTrackedStudent(UUID courseId, UUID userId);

    /** Records that the student opened the item; then updates module progress. */
    StudentContentProgress recordView(Content content, UUID userId);

    /** "Mark as done" (or undo) for an item; then updates module progress. */
    StudentContentProgress markDone(Content content, UUID userId, boolean done);

    /** Every student's progress in a module, for the teacher. */
    List<ModuleStudentProgressDto> moduleProgress(UUID moduleId);
}
