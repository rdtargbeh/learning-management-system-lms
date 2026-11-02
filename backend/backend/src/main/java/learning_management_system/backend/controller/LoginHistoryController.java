package learning_management_system.backend.controller;

import learning_management_system.backend.dto.LoginHistoryDto;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.service.LoginHistoryService;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** login_history: sign-in attempts, own (/auth/login-history) or another user's with USER_READ. */
@RestController
@RequestMapping("/api/v1")
public class LoginHistoryController {
    private final LoginHistoryService history;

    public LoginHistoryController(LoginHistoryService history) { this.history = history; }

    @GetMapping("/auth/login-history")
    public PageResponse<LoginHistoryDto> mine(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return history.list(Access.ctx().userId(), page, size);
    }

    @GetMapping("/users/{userId}/login-history")
    public PageResponse<LoginHistoryDto> forUser(@PathVariable UUID userId, @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "50") int size) {
        return history.list(userId, page, size);
    }
}
