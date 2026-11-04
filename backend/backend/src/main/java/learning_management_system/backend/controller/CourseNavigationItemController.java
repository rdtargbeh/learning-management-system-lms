package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CourseNavigationItemDto;
import learning_management_system.backend.dto.UpdateCourseNavigationRequest;
import learning_management_system.backend.service.CourseNavigationItemService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** course_navigation_item: the course menu. */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/navigation")
public class CourseNavigationItemController {
    private final CourseNavigationItemService navigation;

    public CourseNavigationItemController(CourseNavigationItemService navigation) { this.navigation = navigation; }

    @GetMapping
    public List<CourseNavigationItemDto> list(@PathVariable UUID courseId) { return navigation.list(courseId); }

    @PutMapping
    public List<CourseNavigationItemDto> replace(@PathVariable UUID courseId, @Valid @RequestBody UpdateCourseNavigationRequest r) {
        return navigation.replace(courseId, r);
    }
}
