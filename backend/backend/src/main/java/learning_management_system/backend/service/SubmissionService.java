package learning_management_system.backend.service;

import learning_management_system.backend.dto.GradeSubmissionRequest;
import learning_management_system.backend.dto.StudentSubmissionSummaryDto;
import learning_management_system.backend.dto.SubmissionDto;
import learning_management_system.backend.dto.SubmitAssignmentRequest;

import java.util.List;
import java.util.UUID;

/**
 * Handing in and grading assignments.
 * <ul>
 *   <li>Students submit text or a link (or save a draft) while the assignment is open to them. Lateness is recorded
 *       against their own due date. In a group assignment one submission counts for every member of the group.</li>
 *   <li>Graders (teachers, TAs, graders) see every student's latest attempt, including missing work, and grade it.</li>
 *   <li>With the course's AUTOMATIC post policy grades are visible at once; with MANUAL they stay hidden until posted.</li>
 *   <li>Graders can excuse a student, and reassign an attempt to give the student another try.</li>
 * </ul>
 */
public interface SubmissionService {

    SubmissionDto submit(UUID assignmentId, SubmitAssignmentRequest r);

    /** The caller's own attempts, with feedback once posted. */
    List<SubmissionDto> mine(UUID assignmentId);

    /** Every student of the course with their latest attempt (graders). */
    List<StudentSubmissionSummaryDto> forGrading(UUID assignmentId);

    SubmissionDto get(UUID submissionId);

    SubmissionDto grade(UUID submissionId, GradeSubmissionRequest r);

    /** Makes every graded attempt visible to its student; returns how many were posted. */
    int postGrades(UUID assignmentId);

    SubmissionDto setExcused(UUID assignmentId, UUID userId, boolean excused);

    /** Sends an attempt back: it no longer counts toward the attempt limit, so the student can submit again. */
    SubmissionDto reassign(UUID submissionId);
}
