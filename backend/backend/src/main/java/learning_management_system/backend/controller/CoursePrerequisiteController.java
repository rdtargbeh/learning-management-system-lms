package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CoursePrerequisiteDto;
import learning_management_system.backend.dto.CreateCoursePrerequisiteRequest;
import learning_management_system.backend.dto.PrerequisiteCheckDto;
import learning_management_system.backend.service.CoursePrerequisiteService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** course_prerequisite: a course's prerequisites and a user's standing against them. */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/prerequisites")
public class CoursePrerequisiteController {
    private final CoursePrerequisiteService prerequisites;

    public CoursePrerequisiteController(CoursePrerequisiteService prerequisites) { this.prerequisites = prerequisites; }

    @GetMapping
    public List<CoursePrerequisiteDto> list(@PathVariable UUID courseId) { return prerequisites.list(courseId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoursePrerequisiteDto add(@PathVariable UUID courseId, @Valid @RequestBody CreateCoursePrerequisiteRequest r) {
        return prerequisites.add(courseId, r);
    }

    @DeleteMapping("/{prerequisiteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID courseId, @PathVariable UUID prerequisiteId) { prerequisites.remove(courseId, prerequisiteId); }

    /** userId omitted = the caller. */
    @GetMapping("/check")
    public PrerequisiteCheckDto check(@PathVariable UUID courseId, @RequestParam(required = false) UUID userId) {
        return prerequisites.check(courseId, userId);
    }
}
