package learning_management_system.backend.controller;

import learning_management_system.backend.dto.EmployeeProfileDto;
import learning_management_system.backend.service.EmployeeProfileService;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** employee_profile: faculty, TAs, advisors, staff and admins (employee number, type, department ...). USER_MANAGE edits. */
@RestController
@RequestMapping("/api/v1/users/{userId}/employee-profile")
public class EmployeeProfileController {
    private final EmployeeProfileService employeeProfiles;

    public EmployeeProfileController(EmployeeProfileService employeeProfiles) { this.employeeProfiles = employeeProfiles; }

    @GetMapping
    public EmployeeProfileDto get(@PathVariable UUID userId) { return employeeProfiles.get(userId); }

    @PatchMapping
    public EmployeeProfileDto update(@PathVariable UUID userId, @RequestBody EmployeeProfileDto d) { return employeeProfiles.update(userId, d); }
}
