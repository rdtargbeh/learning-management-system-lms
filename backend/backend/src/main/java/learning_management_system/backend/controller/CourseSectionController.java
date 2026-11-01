package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AssignCourseInstructorRequest;
import learning_management_system.backend.dto.CourseInstructorDto;
import learning_management_system.backend.dto.CourseSectionDto;
import learning_management_system.backend.dto.CreateCourseSectionRequest;
import learning_management_system.backend.dto.CrosslistSectionRequest;
import learning_management_system.backend.dto.UpdateCourseSectionRequest;
import learning_management_system.backend.service.CourseSectionService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Sections, cross-listing and section staff. */
@RestController
@RequestMapping("/api/v1")
public class CourseSectionController {
    private final CourseSectionService sections;

    public CourseSectionController(CourseSectionService sections) { this.sections = sections; }

    @GetMapping("/courses/{courseId}/sections")
    public List<CourseSectionDto> list(@PathVariable UUID courseId) { return sections.list(courseId); }

    @PostMapping("/courses/{courseId}/sections")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseSectionDto create(@PathVariable UUID courseId, @Valid @RequestBody CreateCourseSectionRequest r) {
        return sections.create(courseId, r);
    }

    @GetMapping("/sections/{sectionId}")
    public CourseSectionDto get(@PathVariable UUID sectionId) { return sections.get(sectionId); }

    @PatchMapping("/sections/{sectionId}")
    public CourseSectionDto update(@PathVariable UUID sectionId, @Valid @RequestBody UpdateCourseSectionRequest r) {
        return sections.update(sectionId, r);
    }

    @PostMapping("/sections/{sectionId}/crosslist")
    public CourseSectionDto crosslist(@PathVariable UUID sectionId, @Valid @RequestBody CrosslistSectionRequest r) {
        return sections.crosslist(sectionId, r.targetCourseId());
    }

    @PostMapping("/sections/{sectionId}/uncrosslist")
    public CourseSectionDto uncrosslist(@PathVariable UUID sectionId) { return sections.uncrosslist(sectionId); }

    @GetMapping("/sections/{sectionId}/staff")
    public List<CourseInstructorDto> staff(@PathVariable UUID sectionId) { return sections.staff(sectionId); }

    @PostMapping("/sections/{sectionId}/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseInstructorDto assignStaff(@PathVariable UUID sectionId, @Valid @RequestBody AssignCourseInstructorRequest r) {
        return sections.assignStaff(sectionId, r);
    }

    @DeleteMapping("/sections/{sectionId}/staff/{courseInstructorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeStaff(@PathVariable UUID sectionId, @PathVariable UUID courseInstructorId) {
        sections.removeStaff(sectionId, courseInstructorId);
    }
}
