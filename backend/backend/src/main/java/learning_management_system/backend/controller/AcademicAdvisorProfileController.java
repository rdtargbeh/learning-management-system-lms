package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AcademicAdvisorProfileDto;
import learning_management_system.backend.service.AcademicAdvisorProfileService;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** academic_advisor_profile: advising details of an advisor (who also has an employee profile). USER_MANAGE edits. */
@RestController
@RequestMapping("/api/v1/users/{userId}/advisor-profile")
public class AcademicAdvisorProfileController {
    private final AcademicAdvisorProfileService advisorProfiles;

    public AcademicAdvisorProfileController(AcademicAdvisorProfileService advisorProfiles) { this.advisorProfiles = advisorProfiles; }

    @GetMapping
    public AcademicAdvisorProfileDto get(@PathVariable UUID userId) { return advisorProfiles.get(userId); }

    @PatchMapping
    public AcademicAdvisorProfileDto update(@PathVariable UUID userId, @RequestBody AcademicAdvisorProfileDto d) { return advisorProfiles.update(userId, d); }
}
