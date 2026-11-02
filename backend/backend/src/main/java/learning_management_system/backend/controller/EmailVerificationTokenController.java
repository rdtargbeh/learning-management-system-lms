package learning_management_system.backend.controller;

import learning_management_system.backend.dto.VerifyEmailRequest;
import learning_management_system.backend.service.EmailVerificationTokenService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** email_verification_token: confirm the email address (the request needs sign-in, the link does not). */
@RestController
@RequestMapping("/api/v1/auth/email/verify")
public class EmailVerificationTokenController {
    private final EmailVerificationTokenService verifications;

    public EmailVerificationTokenController(EmailVerificationTokenService verifications) { this.verifications = verifications; }

    @PostMapping("/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void request() { verifications.request(); }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verify(@Valid @RequestBody VerifyEmailRequest r) { verifications.verify(r.token()); }
}
