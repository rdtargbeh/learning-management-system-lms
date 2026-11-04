package learning_management_system.backend.service;

import learning_management_system.backend.dto.CoursePaceDto;
import learning_management_system.backend.dto.CoursePaceRequest;
import learning_management_system.backend.dto.PaceScheduleDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * course_pace: Canvas-style course pacing. A pace lists the module items with a number of working days each; from a
 * student's start (their enrollment date, or the course start when later) it gives every item a due date, skipping
 * weekends and blackout days and compressing to the end date. A published pace sets the due dates of its assignments
 * for the students it covers: a student pace wins over a section pace, which wins over the course pace.
 */
public interface CoursePaceService {

    List<CoursePaceDto> list(UUID courseId);

    /** A draft pace with every module item (1 day each); one draft or published pace per scope. */
    CoursePaceDto create(UUID courseId, CoursePaceRequest r);

    CoursePaceDto get(UUID paceId);

    CoursePaceDto update(UUID paceId, CoursePaceRequest r);

    /** Adds module items that are not in the pace yet and drops items that left their modules. */
    CoursePaceDto refresh(UUID paceId);

    /** Publishing replaces (archives) the scope's earlier published pace. */
    CoursePaceDto publish(UUID paceId);

    CoursePaceDto archive(UUID paceId);

    /** Drafts only. */
    void delete(UUID paceId);

    /** Projected dates for a student (userId), or starting today when userId is null (course staff). */
    PaceScheduleDto schedule(UUID paceId, UUID userId);

    /** The caller's paced schedule in a course (404 when no published pace covers them). */
    PaceScheduleDto mine(UUID courseId);

    /** Internal: the paced due date of an assignment for a student, or null. */
    LocalDate pacedDue(UUID courseId, UUID assignmentId, UUID userId);
}
