package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CreateUserLoginRequest;
import learning_management_system.backend.dto.UserLoginDto;
import learning_management_system.backend.service.UserLoginService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** user_login: extra sign-in names of a user (for example a SIS id). */
@RestController
@RequestMapping("/api/v1/users/{userId}/logins")
public class UserLoginController {
    private final UserLoginService logins;

    public UserLoginController(UserLoginService logins) { this.logins = logins; }

    @GetMapping
    public List<UserLoginDto> list(@PathVariable UUID userId) { return logins.list(userId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserLoginDto add(@PathVariable UUID userId, @Valid @RequestBody CreateUserLoginRequest r) { return logins.add(userId, r); }

    @DeleteMapping("/{loginId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID userId, @PathVariable UUID loginId) { logins.remove(userId, loginId); }
}
