package learning_management_system.backend.service;

import learning_management_system.backend.dto.CourseNavigationItemDto;
import learning_management_system.backend.dto.UpdateCourseNavigationRequest;

import java.util.List;
import java.util.UUID;

/**
 * course_navigation_item: the course menu (Home, Announcements, Modules, ... and external tools). Teachers reorder,
 * rename and hide entries; Home cannot be hidden and Settings is always last and only for staff.
 */
public interface CourseNavigationItemService {

    /** Staff see every entry (hidden ones marked); students and observers see the visible ones. */
    List<CourseNavigationItemDto> list(UUID courseId);

    List<CourseNavigationItemDto> replace(UUID courseId, UpdateCourseNavigationRequest r);
}
