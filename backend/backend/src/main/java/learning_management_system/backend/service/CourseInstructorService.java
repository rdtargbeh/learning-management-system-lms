package learning_management_system.backend.service;

import learning_management_system.backend.dto.AssignCourseInstructorRequest;
import learning_management_system.backend.dto.CourseInstructorDto;

import java.util.List;
import java.util.UUID;

/** course_instructor: who teaches, assists, grades or reviews in a section. Course viewers can see the staff. */
public interface CourseInstructorService {

    List<CourseInstructorDto> list(UUID sectionId);

    /** Course admins (COURSE_MANAGE). Students can only be TAs or graders, observers never; one PRIMARY per section. */
    CourseInstructorDto assign(UUID sectionId, AssignCourseInstructorRequest r);

    void remove(UUID sectionId, UUID courseInstructorId);
}
