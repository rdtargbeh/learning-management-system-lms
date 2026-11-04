package learning_management_system.backend.service;

import learning_management_system.backend.dto.AssignCourseInstructorRequest;
import learning_management_system.backend.dto.CourseInstructorDto;
import learning_management_system.backend.dto.CourseSectionDto;
import learning_management_system.backend.dto.CreateCourseSectionRequest;
import learning_management_system.backend.dto.UpdateCourseSectionRequest;

import java.util.List;
import java.util.UUID;

/**
 * Sections of a course, cross-listing, and section staff (PRIMARY, CO_INSTRUCTOR, TEACHING_ASSISTANT, GRADER,
 * REVIEWER). Creating and changing sections and staff needs COURSE_MANAGE.
 */
public interface CourseSectionService {

    List<CourseSectionDto> list(UUID courseId);

    CourseSectionDto get(UUID sectionId);

    CourseSectionDto create(UUID courseId, CreateCourseSectionRequest r);

    /** Raising the capacity moves students up from the waitlist. */
    CourseSectionDto update(UUID sectionId, UpdateCourseSectionRequest r);

    /** Moves the section, with its enrollments, into another course (Canvas cross-listing). */
    CourseSectionDto crosslist(UUID sectionId, UUID targetCourseId);

    /** Moves a cross-listed section back to the course it came from. */
    CourseSectionDto uncrosslist(UUID sectionId);
}
