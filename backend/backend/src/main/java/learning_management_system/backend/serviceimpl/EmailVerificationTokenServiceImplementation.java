package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.entity.EmailVerificationToken;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.EmailVerificationTokenRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.EmailVerificationTokenService;
import learning_management_system.backend.service.MailService;
import learning_management_system.backend.utility.Tokens;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Supplier;

/** email_verification_token: confirm-your-address links (single use, 24 hours, stored only as SHA-256). */
@Service
public class EmailVerificationTokenServiceImplementation implements EmailVerificationTokenService {
    private static final int VERIFY_HOURS = 24;

    private final UserRepository users;
    private final EmailVerificationTokenRepository verifications;
    private final MailService mail;
    private final AuditLogService audit;
    private final SystemTx sys;

    public EmailVerificationTokenServiceImplementation(UserRepository users, EmailVerificationTokenRepository verifications,
                                                       MailService mail, AuditLogService audit, SystemTx sys) {
        this.users = users;
        this.verifications = verifications;
        this.mail = mail;
        this.audit = audit;
        this.sys = sys;
    }

    @Transactional
    @Override
    public void request() {
        User u = users.findById(Access.ctx().userId()).orElseThrow(() -> ApiException.notFound("User"));
        if (Boolean.TRUE.equals(u.getEmailVerified())) throw ApiException.conflict("ALREADY_VERIFIED", "The email address is already verified");
        OffsetDateTime now = OffsetDateTime.now();
        verifications.revokeOpenForUser(u.getUserId(), now);
        String token = Tokens.random();
        EmailVerificationToken t = new EmailVerificationToken();
        t.setUserId(u.getUserId());
        t.setTenantId(u.getTenantId());
        t.setEmailToVerify(u.getEmail());
        t.setTokenHash(Tokens.sha256(token));
        t.setExpiresAt(now.plusHours(VERIFY_HOURS));
        t.setIsVerified(false);
        t.setAttemptCount(0);
        t.setResendCount(0);
        verifications.saveAndFlush(t);
        mail.send(u.getEmail(), "Confirm your email address", "Confirm this address within " + VERIFY_HOURS + " hours.\nverify-token=" + token);
    }

    @Override
    public void verify(String token) {
        String hash = Tokens.sha256(token.trim());
        EmailVerificationToken found = sys.platform(() -> verifications.findByTokenHash(hash).orElse(null));
        if (found == null) throw ApiException.badRequest("INVALID_TOKEN", "The link is invalid or has expired");
        scope(found.getTenantId(), () -> {
            EmailVerificationToken t = verifications.findById(found.getTokenId()).orElseThrow();
            OffsetDateTime now = OffsetDateTime.now();
            if (Boolean.TRUE.equals(t.getIsVerified()) || t.getRevokedAt() != null || t.getExpiresAt().isBefore(now)) {
                throw ApiException.badRequest("INVALID_TOKEN", "The link is invalid or has expired");
            }
            User u = users.findById(t.getUserId()).orElseThrow();
            if (!u.getEmail().equalsIgnoreCase(t.getEmailToVerify())) {
                throw ApiException.badRequest("EMAIL_CHANGED", "The account's email address changed; ask for a new link");
            }
            u.setEmailVerified(true);
            u.setDateEmailVerified(now);
            users.saveAndFlush(u);
            t.setIsVerified(true);
            t.setVerifiedAt(now);
            verifications.saveAndFlush(t);
            return null;
        });
        audit.recordFor(found.getTenantId(), found.getUserId(), "EMAIL_VERIFIED", "USER", found.getUserId(), Severity.INFO);
    }

    private <T> T scope(UUID tenantId, Supplier<T> work) {
        return tenantId == null ? sys.platform(work) : sys.inTenant(tenantId, work);
    }
}
