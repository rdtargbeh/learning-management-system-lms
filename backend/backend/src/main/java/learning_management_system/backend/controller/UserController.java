package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AcademicAdvisorProfileDto;
import learning_management_system.backend.dto.AssignRoleRequest;
import learning_management_system.backend.dto.CreateUserRequest;
import learning_management_system.backend.dto.EmployeeProfileDto;
import learning_management_system.backend.dto.ObserverStudentLinkDto;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.SetPasswordRequest;
import learning_management_system.backend.dto.StudentProfileDto;
import learning_management_system.backend.dto.UpdateUserRequest;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.dto.UserProfileDto;
import learning_management_system.backend.dto.UserRoleDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.service.ObserverStudentLinkService;
import learning_management_system.backend.service.RoleService;
import learning_management_system.backend.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Institution users and their profiles (tenant context required). */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService users;

    public UserController(UserService users) { this.users = users; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDetailDto create(@Valid @RequestBody CreateUserRequest r) { return users.create(r); }

    @GetMapping
    public PageResponse<UserSummaryDto> search(@RequestParam(required = false) UserType type,
                                            @RequestParam(required = false) AccountStatus status,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "50") int size) {
        return users.search(type, status, q, page, size);
    }

    @GetMapping("/{userId}")
    public UserDetailDto get(@PathVariable UUID userId) { return users.get(userId); }

    @PatchMapping("/{userId}")
    public UserDetailDto update(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest r) { return users.update(userId, r); }

    /** Users with academic records cannot be hard-deleted; deactivate them instead. */
    @PostMapping("/{userId}/deactivate")
    public UserDetailDto deactivate(@PathVariable UUID userId) { return users.setStatus(userId, AccountStatus.DEACTIVATED); }

    @PostMapping("/{userId}/reactivate")
    public UserDetailDto reactivate(@PathVariable UUID userId) { return users.setStatus(userId, AccountStatus.ACTIVE); }

    @PostMapping("/{userId}/suspend")
    public UserDetailDto suspend(@PathVariable UUID userId) { return users.setStatus(userId, AccountStatus.SUSPENDED); }

    @PutMapping("/{userId}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPassword(@PathVariable UUID userId, @Valid @RequestBody SetPasswordRequest r) { users.setPassword(userId, r); }
}
