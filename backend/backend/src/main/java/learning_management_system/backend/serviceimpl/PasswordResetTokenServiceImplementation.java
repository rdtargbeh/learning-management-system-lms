package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.ForgotPasswordRequest;
import learning_management_system.backend.dto.ResetPasswordRequest;
import learning_management_system.backend.entity.PasswordResetToken;
import learning_management_system.backend.entity.Tenant;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.enums.TenantStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.PasswordResetTokenRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.AccountLockService;
import learning_management_system.backend.service.PasswordResetTokenService;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.MailService;
import learning_management_system.backend.service.UserSessionService;
import learning_management_system.backend.utility.Tokens;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * password_reset_token: "forgot password" links. Tokens are random, stored only as SHA-256, single use, expire after
 * 30 minutes and allow 10 attempts. Lookups run in a trusted context: the caller is not signed in.
 */
@Service
public class PasswordResetTokenServiceImplementation implements PasswordResetTokenService {
    private static final int RESET_MINUTES = 30;
    private static final int MAX_ATTEMPTS = 10;

    private final UserRepository users;
    private final TenantRepository tenants;
    private final PasswordResetTokenRepository resets;
    private final UserSessionService sessions;
    private final AccountLockService locks;
    private final MailService mail;
    private final AuditLogService audit;
    private final PasswordEncoder encoder;
    private final SystemTx sys;

    public PasswordResetTokenServiceImplementation(UserRepository users, TenantRepository tenants, PasswordResetTokenRepository resets,
                                                   UserSessionService sessions, AccountLockService locks, MailService mail,
                                                   AuditLogService audit, PasswordEncoder encoder, SystemTx sys) {
        this.users = users;
        this.tenants = tenants;
        this.resets = resets;
        this.sessions = sessions;
        this.locks = locks;
        this.mail = mail;
        this.audit = audit;
        this.encoder = encoder;
        this.sys = sys;
    }

    @Override
    public void forgot(ForgotPasswordRequest r, InetAddress ip, String userAgent) {
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
    public void reset(ResetPasswordRequest r) {
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

    private <T> T scope(UUID tenantId, Supplier<T> work) {
        return tenantId == null ? sys.platform(work) : sys.inTenant(tenantId, work);
    }
}
