package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AssignCourseInstructorRequest;
import learning_management_system.backend.dto.CourseInstructorDto;
import learning_management_system.backend.service.CourseInstructorService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** course_instructor: a section's staff. */
@RestController
@RequestMapping("/api/v1/sections/{sectionId}/staff")
public class CourseInstructorController {
    private final CourseInstructorService staff;

    public CourseInstructorController(CourseInstructorService staff) { this.staff = staff; }

    @GetMapping
    public List<CourseInstructorDto> list(@PathVariable UUID sectionId) { return staff.list(sectionId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseInstructorDto assign(@PathVariable UUID sectionId, @Valid @RequestBody AssignCourseInstructorRequest r) {
        return staff.assign(sectionId, r);
    }

    @DeleteMapping("/{courseInstructorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID sectionId, @PathVariable UUID courseInstructorId) { staff.remove(sectionId, courseInstructorId); }
}
