package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.ForgotPasswordRequest;
import learning_management_system.backend.dto.ResetPasswordRequest;
import learning_management_system.backend.entity.EmailVerificationToken;
import learning_management_system.backend.entity.PasswordResetToken;
import learning_management_system.backend.entity.Tenant;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.enums.TenantStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.EmailVerificationTokenRepository;
import learning_management_system.backend.repository.PasswordResetTokenRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.AccountLockService;
import learning_management_system.backend.service.AccountRecoveryService;
import learning_management_system.backend.service.AuditService;
import learning_management_system.backend.service.MailService;
import learning_management_system.backend.service.SessionService;
import learning_management_system.backend.utility.Tokens;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class AccountRecoveryServiceImplementation implements AccountRecoveryService {
    private static final int RESET_MINUTES = 30;
    private static final int VERIFY_HOURS = 24;
    private static final int MAX_ATTEMPTS = 10;

    private final UserRepository users;
    private final TenantRepository tenants;
    private final PasswordResetTokenRepository resets;
    private final EmailVerificationTokenRepository verifications;
    private final SessionService sessions;
    private final AccountLockService locks;
    private final MailService mail;
    private final AuditService audit;
    private final PasswordEncoder encoder;
    private final SystemTx sys;

    public AccountRecoveryServiceImplementation(UserRepository users, TenantRepository tenants, PasswordResetTokenRepository resets,
                                                EmailVerificationTokenRepository verifications, SessionService sessions,
                                                AccountLockService locks, MailService mail, AuditService audit,
                                                PasswordEncoder encoder, SystemTx sys) {
        this.users = users;
        this.tenants = tenants;
        this.resets = resets;
        this.verifications = verifications;
        this.sessions = sessions;
        this.locks = locks;
        this.mail = mail;
        this.audit = audit;
        this.encoder = encoder;
        this.sys = sys;
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest r, InetAddress ip, String userAgent) {
        String login = r.login().trim();
        UUID tenantId;
        if (r.tenantCode() == null || r.tenantCode().isBlank()) {
            tenantId = null;
        } else {
            Tenant t = sys.platform(() -> tenants.findByTenantCodeIgnoreCase(r.tenantCode().trim()).orElse(null));
            if (t == null || t.getTenantStatus() != TenantStatus.ACTIVE) return;          // same answer either way
            tenantId = t.getTenantId();
        }
        scope(tenantId, () -> {
            User u = (tenantId == null
                    ? users.findByTenantIdIsNullAndUsernameIgnoreCase(login).or(() -> users.findByTenantIdIsNullAndEmail(login))
                    : users.findByTenantIdAndUsernameIgnoreCase(tenantId, login).or(() -> users.findByTenantIdAndEmail(tenantId, login)))
                    .orElse(null);
            if (u == null || u.getAccountStatus() == AccountStatus.DEACTIVATED || u.getAccountStatus() == AccountStatus.DELETED) return null;
            OffsetDateTime now = OffsetDateTime.now();
            resets.revokeOpenForUser(u.getUserId(), now);
            String token = Tokens.random();
            PasswordResetToken t = new PasswordResetToken();
            t.setUserId(u.getUserId());
            t.setTenantId(u.getTenantId());
            t.setTokenHash(Tokens.sha256(token));
            t.setRequestIp(ip);
            t.setRequestUserAgent(userAgent);
            t.setAttemptCount(0);
            t.setIsUsed(false);
            t.setDateExpires(now.plusMinutes(RESET_MINUTES));
            resets.saveAndFlush(t);
            mail.send(u.getEmail(), "Reset your password",
                    "Use this code within " + RESET_MINUTES + " minutes to choose a new password.\nreset-token=" + token);
            return null;
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest r) {
        String hash = Tokens.sha256(r.token().trim());
        PasswordResetToken found = sys.platform(() -> resets.findByTokenHash(hash).orElse(null));
        if (found == null) throw ApiException.badRequest("INVALID_TOKEN", "The link is invalid or has expired");
        UUID tenantId = found.getTenantId();
        scope(tenantId, () -> {
            PasswordResetToken t = resets.findById(found.getTokenId()).orElseThrow();
            OffsetDateTime now = OffsetDateTime.now();
            if (Boolean.TRUE.equals(t.getIsUsed()) || t.getRevokedAt() != null || t.getDateExpires().isBefore(now)
                    || (t.getAttemptCount() != null && t.getAttemptCount() >= MAX_ATTEMPTS)) {
                throw ApiException.badRequest("INVALID_TOKEN", "The link is invalid or has expired");
            }
            User u = users.findById(t.getUserId()).orElseThrow(() -> ApiException.badRequest("INVALID_TOKEN", "The link is invalid or has expired"));
            if (u.getPasswordHash() != null && encoder.matches(r.newPassword(), u.getPasswordHash())) {
                t.setAttemptCount((t.getAttemptCount() == null ? 0 : t.getAttemptCount()) + 1);
                resets.saveAndFlush(t);
                throw ApiException.badRequest("SAME_PASSWORD", "Choose a password you have not used for this account");
            }
            u.setPasswordHash(encoder.encode(r.newPassword()));
            u.setPasswordLastChanged(now);
            u.setMustChangePassword(false);
            u.setFailedLoginAttempts(0);
            u.setAccountLockedUntil(null);
            if (u.getAccountStatus() == AccountStatus.INVITED) u.setAccountStatus(AccountStatus.ACTIVE);
            users.saveAndFlush(u);
            t.setIsUsed(true);
            t.setDateUsed(now);
            resets.saveAndFlush(t);
            locks.clearAutomaticLock(u.getUserId());
            sessions.revokeAll(u.getUserId());
            return null;
        });
        audit.recordFor(tenantId, found.getUserId(), "PASSWORD_RESET", "USER", found.getUserId(), Severity.WARNING);
    }

    @Transactional
    @Override
    public void requestEmailVerification() {
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
    public void verifyEmail(String token) {
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
    }

    private <T> T scope(UUID tenantId, Supplier<T> work) {
        return tenantId == null ? sys.platform(work) : sys.inTenant(tenantId, work);
    }
}
