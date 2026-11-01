package learning_management_system.backend.controller;

import learning_management_system.backend.dto.ChangePasswordRequest;
import learning_management_system.backend.dto.LoginRequest;
import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    /** Platform staff: omit tenantCode. Institution users: send the institution's tenantCode. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r, HttpServletRequest http) {
        return auth.login(r, clientIp(http));
    }

    /** The signed-in user with all of their profiles. */
    @GetMapping("/me")
    public UserDetailDto me() {
        return auth.me();
    }

    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest r) {
        auth.changeOwnPassword(r);
    }

    private static InetAddress clientIp(HttpServletRequest http) {
        try {
            return InetAddress.getByName(http.getRemoteAddr());
        } catch (Exception e) {
            return null;
        }
    }
}
