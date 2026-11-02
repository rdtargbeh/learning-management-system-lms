package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.ChangePasswordRequest;
import learning_management_system.backend.dto.LoginRequest;
import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.MfaVerifyRequest;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.entity.PlatformUserProfile;
import learning_management_system.backend.entity.Tenant;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.LoginMethod;
import learning_management_system.backend.enums.LoginStatus;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.enums.TenantStatus;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.IdentityMapper;
import learning_management_system.backend.service.LoginHistoryService;
import learning_management_system.backend.repository.PlatformUserProfileRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.TenantSettingsRepository;
import learning_management_system.backend.repository.UserLoginRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.security.TokenService;
import learning_management_system.backend.service.AccountLockService;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.AuthService;
import learning_management_system.backend.service.MfaConfigurationService;
import learning_management_system.backend.service.UserSessionService;
import learning_management_system.backend.service.UserService;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class AuthServiceImplementation implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final SystemTx sys;
    private final UserRepository users;
    private final TenantRepository tenants;
    private final TenantSettingsRepository tenantSettings;
    private final UserLoginRepository userLogins;
    private final PlatformUserProfileRepository platformProfiles;
    private final LoginHistoryService history;
    private final PasswordEncoder encoder;
    private final TokenService tokens;
    private final UserService userService;
    private final UserSessionService sessions;
    private final MfaConfigurationService mfa;
    private final AccountLockService locks;
    private final AuditLogService audit;
    private final IdentityMapper identityMapper;

    public AuthServiceImplementation(SystemTx sys, UserRepository users, TenantRepository tenants, TenantSettingsRepository tenantSettings,
                                     UserLoginRepository userLogins, PlatformUserProfileRepository platformProfiles,
                                     LoginHistoryService history, PasswordEncoder encoder, TokenService tokens,
                                     UserService userService, UserSessionService sessions, MfaConfigurationService mfa, AccountLockService locks,
                                     AuditLogService audit, IdentityMapper identityMapper) {
        this.sys = sys;
        this.users = users;
        this.tenants = tenants;
        this.tenantSettings = tenantSettings;
        this.userLogins = userLogins;
        this.platformProfiles = platformProfiles;
        this.history = history;
        this.encoder = encoder;
        this.tokens = tokens;
        this.userService = userService;
        this.sessions = sessions;
        this.mfa = mfa;
        this.locks = locks;
        this.audit = audit;
        this.identityMapper = identityMapper;
    }

    // ---------------------------------------------------------------- sign-in

    /** Lookups run in a trusted system context because RLS hides users until the tenant/user is known. */
    @Override
    public LoginResponse login(LoginRequest r, InetAddress ip, String userAgent) {
        String login = r.login().trim();
        UUID tenantId;
        User user;
        if (r.tenantCode() == null || r.tenantCode().isBlank()) {
            tenantId = null;
            user = sys.platform(() -> users.findByTenantIdIsNullAndUsernameIgnoreCase(login)
                    .or(() -> users.findByTenantIdIsNullAndEmail(login)).orElse(null));
        } else {
            Tenant tenant = sys.platform(() -> tenants.findByTenantCodeIgnoreCase(r.tenantCode().trim()).orElse(null));
            if (tenant == null || tenant.getTenantStatus() != TenantStatus.ACTIVE || !Boolean.TRUE.equals(tenant.getIsActive())) {
                throw ApiException.unauthorized("Invalid credentials");
            }
            tenantId = tenant.getTenantId();
            user = sys.inTenant(tenantId, () -> users.findByTenantIdAndUsernameIgnoreCase(tenantId, login)
                    .or(() -> users.findByTenantIdAndEmail(tenantId, login))
                    .or(() -> userLogins.findFirstByTenantIdAndUniqueLoginIgnoreCaseAndIsActiveTrue(tenantId, login)
                            .flatMap(l -> users.findById(l.getUserId())))
                    .orElse(null));
        }
        if (user == null) {
            history.record(null, tenantId, login, ip, LoginStatus.FAILED, "unknown login", false);
            throw ApiException.unauthorized("Invalid credentials");
        }
        final UUID userId = user.getUserId();
        requireMaySignIn(user, tenantId, login, ip);
        if (user.getPasswordHash() == null || !encoder.matches(r.password(), user.getPasswordHash())) {
            onFailure(userId, tenantId, ip);
            history.record(userId, tenantId, login, ip, LoginStatus.FAILED, "bad password", false);
            throw ApiException.unauthorized("Invalid credentials");
        }
        if (inUserScope(tenantId, () -> mfa.isEnabled(userId))) {
            inUserScope(tenantId, () -> {                     // the password was right: forget earlier misses
                User u = users.findById(userId).orElseThrow();
                u.setFailedLoginAttempts(0);
                return users.save(u);
            });
            history.record(userId, tenantId, login, ip, LoginStatus.MFA_REQUIRED, null, false);
            return new LoginResponse(null, null, tokens.mfaPendingSeconds(), null, null, null, null, List.of(), null, null,
                    true, tokens.issueMfaPending(userId, tenantId), false);
        }
        return complete(userId, tenantId, login, ip, userAgent, false);
    }

    @Override
    public LoginResponse verifyMfa(MfaVerifyRequest r, InetAddress ip, String userAgent) {
        UUID[] ids;
        try {
            ids = tokens.parseMfaPending(r.mfaToken());
        } catch (JwtException | IllegalArgumentException e) {
            throw ApiException.unauthorized("Sign in again");
        }
        UUID userId = ids[0], tenantId = ids[1];
        User user = inUserScope(tenantId, () -> users.findById(userId).orElse(null));
        if (user == null) throw ApiException.unauthorized("Sign in again");
        requireMaySignIn(user, tenantId, user.getUsername(), ip);
        if (!inUserScope(tenantId, () -> mfa.verifyLoginCode(userId, r.code()))) {
            history.record(userId, tenantId, user.getUsername(), ip, LoginStatus.FAILED, "bad MFA code", true);
            throw ApiException.unauthorized("The code is not correct");
        }
        return complete(userId, tenantId, user.getUsername(), ip, userAgent, true);
    }

    /** Lock and status checks shared by both sign-in steps. */
    private void requireMaySignIn(User user, UUID tenantId, String login, InetAddress ip) {
        UUID userId = user.getUserId();
        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(OffsetDateTime.now())) {
            history.record(userId, tenantId, login, ip, LoginStatus.BLOCKED, "account locked", false);
            throw new ApiException(HttpStatus.LOCKED, "ACCOUNT_LOCKED", "Account is temporarily locked; try again later");
        }
        if (user.getAccountStatus() == AccountStatus.LOCKED && inUserScope(tenantId, () -> locks.stillLocked(userId))) {
            history.record(userId, tenantId, login, ip, LoginStatus.BLOCKED, "locked by an administrator", false);
            throw new ApiException(HttpStatus.LOCKED, "ACCOUNT_LOCKED", "Account is locked; contact your administrator");
        }
        User fresh = inUserScope(tenantId, () -> users.findById(userId).orElseThrow());
        if (fresh.getAccountStatus() != AccountStatus.ACTIVE) {
            history.record(userId, tenantId, login, ip, LoginStatus.BLOCKED, "status " + fresh.getAccountStatus(), false);
            throw ApiException.unauthorized("Account is not active");
        }
    }

    private LoginResponse complete(UUID userId, UUID tenantId, String login, InetAddress ip, String userAgent, boolean mfaUsed) {
        LoginResponse resp = inUserScope(tenantId, () -> {
            User u = users.findById(userId).orElseThrow();
            u.setFailedLoginAttempts(0);
            u.setAccountLockedUntil(null);
            u.setLastLogin(OffsetDateTime.now());
            u.setLoginCount(u.getLoginCount() == null ? 1 : u.getLoginCount() + 1);
            users.save(u);
            boolean mfaOn = mfa.isEnabled(userId);
            boolean tenantRequires = tenantId != null && tenantSettings.findByTenantId(tenantId)
                    .map(s -> Boolean.TRUE.equals(s.getRequireMfa())).orElse(false);
            boolean setupRequired = !mfaOn && (Boolean.TRUE.equals(u.getMfaRequired()) || tenantRequires);
            return sessions.start(u, tenantId, ip, userAgent, setupRequired);
        });
        history.record(userId, tenantId, login, ip, LoginStatus.SUCCESS, null, mfaUsed);
        return resp;
    }

    private void onFailure(UUID userId, UUID tenantId, InetAddress ip) {
        inUserScope(tenantId, () -> {
            User u = users.findById(userId).orElseThrow();
            int failed = (u.getFailedLoginAttempts() == null ? 0 : u.getFailedLoginAttempts()) + 1;
            u.setFailedLoginAttempts(failed);
            u.setLastFailedLogin(OffsetDateTime.now());
            if (failed >= MAX_FAILED_ATTEMPTS) {
                OffsetDateTime until = OffsetDateTime.now().plusMinutes(LOCK_MINUTES);
                u.setAccountLockedUntil(until);
                u.setFailedLoginAttempts(0);
                users.save(u);
                locks.recordAutomaticLock(userId, failed, ip, until);
                return u;
            }
            return users.save(u);
        });
    }


    private <T> T inUserScope(UUID tenantId, Supplier<T> work) {
        return tenantId == null ? sys.platform(work) : sys.inTenant(tenantId, work);
    }

    // ---------------------------------------------------------------- own account

    @Transactional(readOnly = true)
    @Override
    public UserDetailDto me() {
        return userService.toDetail(users.findById(Access.ctx().userId()).orElseThrow(() -> ApiException.notFound("User")));
    }

    @Transactional
    @Override
    public void changeOwnPassword(ChangePasswordRequest r) {
        UUID me = Access.ctx().userId();
        User u = users.findById(me).orElseThrow(() -> ApiException.notFound("User"));
        if (u.getPasswordHash() == null || !encoder.matches(r.currentPassword(), u.getPasswordHash())) {
            throw ApiException.badRequest("WRONG_PASSWORD", "Current password is incorrect");
        }
        if (encoder.matches(r.newPassword(), u.getPasswordHash())) {
            throw ApiException.badRequest("SAME_PASSWORD", "Choose a new password");
        }
        u.setPasswordHash(encoder.encode(r.newPassword()));
        u.setPasswordLastChanged(OffsetDateTime.now());
        u.setMustChangePassword(false);
        users.save(u);
        UUID current = Access.currentSessionId();
        sessions.mine().stream().filter(s -> !s.sessionId().equals(current)).forEach(s -> sessions.revoke(s.sessionId()));
        audit.record("PASSWORD_CHANGED", "USER", me, me, Severity.INFO, null, null);
    }


    // ---------------------------------------------------------------- support sessions

    @Override
    public String supportToken(UUID platformUserId, UUID tenantId, UUID supportAccessId) {
        String platformRole = sys.platform(() -> platformProfiles.findByUserId(platformUserId)
                .map(PlatformUserProfile::getPlatformRole).map(Enum::name).orElse(null));
        return tokens.issue(platformUserId, tenantId, UserType.PLATFORM.name(), true, platformRole, List.of(), supportAccessId);
    }

    @Override
    public long ttlSeconds() { return tokens.ttlSeconds(); }
}
