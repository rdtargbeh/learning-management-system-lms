package learning_management_system.backend.service;

import learning_management_system.backend.dto.CourseDto;
import learning_management_system.backend.dto.CreateCourseRequest;
import learning_management_system.backend.dto.MyCourseDto;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.SelfEnrollmentSettingsRequest;
import learning_management_system.backend.dto.UpdateCourseRequest;
import learning_management_system.backend.enums.CourseStatus;

import java.util.List;
import java.util.UUID;

/**
 * Courses: create (COURSE_MANAGE), catalog search (COURSE_READ), the caller's own courses, and the course life cycle
 * UNPUBLISHED -> PUBLISHED -> CONCLUDED -> ARCHIVED. Teachers can edit, publish and conclude their own course;
 * archiving needs COURSE_MANAGE. Students see a course only while it is published or concluded.
 */
public interface CourseService {

    CourseDto create(CreateCourseRequest r);

    PageResponse<CourseDto> search(UUID termId, CourseStatus status, UUID categoryId, UUID orgUnitId, String q, int page, int size);

    /** Courses the caller teaches or is enrolled in (not archived), for the dashboard. */
    List<MyCourseDto> mine();

    CourseDto get(UUID courseId);

    CourseDto update(UUID courseId, UpdateCourseRequest r);

    CourseDto changeStatus(UUID courseId, CourseStatus status);

    /** Turns self-enrollment by code on or off; the returned course carries the code. */
    CourseDto selfEnrollment(UUID courseId, SelfEnrollmentSettingsRequest r);
}
