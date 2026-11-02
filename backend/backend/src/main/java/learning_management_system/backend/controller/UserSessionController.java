package learning_management_system.backend.controller;

import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.RefreshRequest;
import learning_management_system.backend.dto.SessionDto;
import learning_management_system.backend.service.UserSessionService;
import learning_management_system.backend.utility.ClientInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** user_session: refresh, sign out, and the caller's signed-in devices. */
@RestController
@RequestMapping("/api/v1/auth")
public class UserSessionController {
    private final UserSessionService sessions;

    public UserSessionController(UserSessionService sessions) { this.sessions = sessions; }

    /** No access token needed: a valid refresh token gives a new access token and a new refresh token. */
    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest r, HttpServletRequest http) {
        return sessions.refresh(r.refreshToken(), ClientInfo.ip(http), ClientInfo.userAgent(http));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() { sessions.logout(); }

    @GetMapping("/sessions")
    public List<SessionDto> mine() { return sessions.mine(); }

    /** Sign out one device (own sessions; others' with USER_MANAGE). */
    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable UUID sessionId) { sessions.revoke(sessionId); }
}
