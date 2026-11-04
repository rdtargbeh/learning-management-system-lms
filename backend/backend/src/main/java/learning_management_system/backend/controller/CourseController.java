package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CourseDto;
import learning_management_system.backend.dto.CreateCourseRequest;
import learning_management_system.backend.dto.MyCourseDto;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.SelfEnrollmentSettingsRequest;
import learning_management_system.backend.dto.UpdateCourseRequest;
import learning_management_system.backend.enums.CourseStatus;
import learning_management_system.backend.service.CourseService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Courses and their life cycle. */
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {
    private final CourseService courses;

    public CourseController(CourseService courses) { this.courses = courses; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDto create(@Valid @RequestBody CreateCourseRequest r) { return courses.create(r); }

    /** Catalog search (COURSE_READ). */
    @GetMapping
    public PageResponse<CourseDto> search(@RequestParam(required = false) UUID termId,
                                          @RequestParam(required = false) CourseStatus status,
                                          @RequestParam(required = false) UUID categoryId,
                                          @RequestParam(required = false) UUID orgUnitId,
                                          @RequestParam(required = false) String q,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "50") int size) {
        return courses.search(termId, status, categoryId, orgUnitId, q, page, size);
    }

    /** The caller's dashboard: courses they teach or take. */
    @GetMapping("/mine")
    public List<MyCourseDto> mine() { return courses.mine(); }

    @GetMapping("/{courseId}")
    public CourseDto get(@PathVariable UUID courseId) { return courses.get(courseId); }

    @PatchMapping("/{courseId}")
    public CourseDto update(@PathVariable UUID courseId, @Valid @RequestBody UpdateCourseRequest r) { return courses.update(courseId, r); }

    @PostMapping("/{courseId}/publish")
    public CourseDto publish(@PathVariable UUID courseId) { return courses.changeStatus(courseId, CourseStatus.PUBLISHED); }

    @PostMapping("/{courseId}/unpublish")
    public CourseDto unpublish(@PathVariable UUID courseId) { return courses.changeStatus(courseId, CourseStatus.UNPUBLISHED); }

    @PostMapping("/{courseId}/conclude")
    public CourseDto conclude(@PathVariable UUID courseId) { return courses.changeStatus(courseId, CourseStatus.CONCLUDED); }

    @PostMapping("/{courseId}/archive")
    public CourseDto archive(@PathVariable UUID courseId) { return courses.changeStatus(courseId, CourseStatus.ARCHIVED); }

    /** Brings an archived course back as unpublished. */
    @PostMapping("/{courseId}/unarchive")
    public CourseDto unarchive(@PathVariable UUID courseId) { return courses.changeStatus(courseId, CourseStatus.UNPUBLISHED); }

    @PutMapping("/{courseId}/self-enrollment")
    public CourseDto selfEnrollment(@PathVariable UUID courseId, @Valid @RequestBody SelfEnrollmentSettingsRequest r) {
        return courses.selfEnrollment(courseId, r);
    }
}
