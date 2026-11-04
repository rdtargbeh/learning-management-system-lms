package learning_management_system.backend.controller;

import learning_management_system.backend.dto.StudentProfileDto;
import learning_management_system.backend.service.StudentProfileService;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** student_profile: a student's academic record fields (student number, program, standing ...). USER_MANAGE edits. */
@RestController
@RequestMapping("/api/v1/users/{userId}/student-profile")
public class StudentProfileController {
    private final StudentProfileService studentProfiles;

    public StudentProfileController(StudentProfileService studentProfiles) { this.studentProfiles = studentProfiles; }

    @GetMapping
    public StudentProfileDto get(@PathVariable UUID userId) { return studentProfiles.get(userId); }

    @PatchMapping
    public StudentProfileDto update(@PathVariable UUID userId, @RequestBody StudentProfileDto d) { return studentProfiles.update(userId, d); }
}
