package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CreateEnrollmentRequest;
import learning_management_system.backend.dto.EnrollmentDto;
import learning_management_system.backend.dto.SelfEnrollRequest;
import learning_management_system.backend.dto.UpdateEnrollmentStatusRequest;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.EnrollmentType;
import learning_management_system.backend.service.EnrollmentService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Enrollments and section rosters. */
@RestController
@RequestMapping("/api/v1")
public class EnrollmentController {
    private final EnrollmentService enrollments;

    public EnrollmentController(EnrollmentService enrollments) { this.enrollments = enrollments; }

    @PostMapping("/sections/{sectionId}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentDto enroll(@PathVariable UUID sectionId, @Valid @RequestBody CreateEnrollmentRequest r) {
        return enrollments.enroll(sectionId, r);
    }

    @GetMapping("/sections/{sectionId}/enrollments")
    public List<EnrollmentDto> roster(@PathVariable UUID sectionId, @RequestParam(required = false) EnrollmentStatus status,
                                      @RequestParam(required = false) EnrollmentType type) {
        return enrollments.roster(sectionId, status, type);
    }

    /** A student joins a course with its self-enrollment code. */
    @PostMapping("/enrollments/self")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentDto selfEnroll(@Valid @RequestBody SelfEnrollRequest r) { return enrollments.selfEnroll(r.code()); }

    @GetMapping("/enrollments/{enrollmentId}")
    public EnrollmentDto get(@PathVariable UUID enrollmentId) { return enrollments.get(enrollmentId); }

    @PatchMapping("/enrollments/{enrollmentId}/status")
    public EnrollmentDto changeStatus(@PathVariable UUID enrollmentId, @Valid @RequestBody UpdateEnrollmentStatusRequest r) {
        return enrollments.changeStatus(enrollmentId, r.status());
    }

    @GetMapping("/users/{userId}/enrollments")
    public List<EnrollmentDto> forUser(@PathVariable UUID userId) { return enrollments.forUser(userId); }
}
