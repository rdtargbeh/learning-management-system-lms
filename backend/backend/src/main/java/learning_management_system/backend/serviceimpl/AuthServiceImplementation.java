package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.ChangePasswordRequest;
import learning_management_system.backend.dto.LoginRequest;
import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.LoginHistory;
import learning_management_system.backend.entity.PlatformUserProfile;
import learning_management_system.backend.entity.Tenant;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.entity.UserRole;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.LoginMethod;
import learning_management_system.backend.enums.LoginStatus;
import learning_management_system.backend.enums.TenantStatus;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.LoginHistoryRepository;
import learning_management_system.backend.repository.PlatformUserProfileRepository;
import learning_management_system.backend.repository.RolePermissionRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.repository.UserRoleRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.security.TokenService;
import learning_management_system.backend.service.AuthService;
import learning_management_system.backend.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Password sign-in for platform staff (no tenantCode) and institution users (tenantCode required).
 * Lookups run in a trusted system context because RLS hides users until the tenant/user is known.
 */
@Service
public class AuthServiceImplementation implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final SystemTx sys;
    private final UserRepository users;
    private final TenantRepository tenants;
    private final UserRoleRepository userRoles;
    private final RolePermissionRepository rolePermissions;
    private final PlatformUserProfileRepository platformProfiles;
    private final LoginHistoryRepository loginHistory;
    private final PasswordEncoder encoder;
    private final TokenService tokens;
    private final UserService userService;

    public AuthServiceImplementation(SystemTx sys, UserRepository users, TenantRepository tenants,
                                     UserRoleRepository userRoles, RolePermissionRepository rolePermissions,
                                     PlatformUserProfileRepository platformProfiles, LoginHistoryRepository loginHistory,
                                     PasswordEncoder encoder, TokenService tokens, UserService userService) {
        this.sys = sys;
        this.users = users;
        this.tenants = tenants;
        this.userRoles = userRoles;
        this.rolePermissions = rolePermissions;
        this.platformProfiles = platformProfiles;
        this.loginHistory = loginHistory;
        this.encoder = encoder;
        this.tokens = tokens;
        this.userService = userService;
    }

    @Override
    public LoginResponse login(LoginRequest r, InetAddress ip) {
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
                    .or(() -> users.findByTenantIdAndEmail(tenantId, login)).orElse(null));
        }
        if (user == null) {
            record(null, tenantId, login, ip, LoginStatus.FAILED, "unknown login");
            throw ApiException.unauthorized("Invalid credentials");
        }
        final UUID userId = user.getUserId();
        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(OffsetDateTime.now())) {
            record(userId, tenantId, login, ip, LoginStatus.BLOCKED, "account locked");
            throw new ApiException(HttpStatus.LOCKED, "ACCOUNT_LOCKED", "Account is temporarily locked; try again later");
        }
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            record(userId, tenantId, login, ip, LoginStatus.BLOCKED, "status " + user.getAccountStatus());
            throw ApiException.unauthorized("Account is not active");
        }
        if (user.getPasswordHash() == null || !encoder.matches(r.password(), user.getPasswordHash())) {
            onFailure(userId, tenantId);
            record(userId, tenantId, login, ip, LoginStatus.FAILED, "bad password");
            throw ApiException.unauthorized("Invalid credentials");
        }

        User updated = inUserScope(tenantId, () -> {
            User u = users.findById(userId).orElseThrow();
            u.setFailedLoginAttempts(0);
            u.setAccountLockedUntil(null);
            u.setLastLogin(OffsetDateTime.now());
            u.setLoginCount(u.getLoginCount() == null ? 1 : u.getLoginCount() + 1);
            return users.save(u);
        });
        record(userId, tenantId, login, ip, LoginStatus.SUCCESS, null);

        List<String> perms = inUserScope(tenantId, () -> permissionsOf(userId));
        String platformRole = Boolean.TRUE.equals(updated.getIsSystemUser())
                ? sys.platform(() -> platformProfiles.findByUserId(userId).map(p -> p.getPlatformRole().name()).orElse(null))
                : null;
        String token = tokens.issue(userId, tenantId, updated.getUserType().name(), Boolean.TRUE.equals(updated.getIsSystemUser()),
                platformRole, perms, null);
        UserSummaryDto summary = inUserScope(tenantId, () -> userService.toSummary(updated));
        return new LoginResponse(token, "Bearer", tokens.ttlSeconds(), summary, perms, platformRole, null);
    }

    /** Permission codes granted through the user's roles. */
    private List<String> permissionsOf(UUID userId) {
        List<UUID> roleIds = userRoles.findByUserId(userId).stream().map(UserRole::getRoleId).toList();
        return roleIds.isEmpty() ? List.of() : rolePermissions.findPermissionCodesForRoles(roleIds).stream().distinct().sorted().toList();
    }

    /** The signed-in user with all of their profiles. */
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
        u.setPasswordHash(encoder.encode(r.newPassword()));
        u.setPasswordLastChanged(OffsetDateTime.now());
        u.setMustChangePassword(false);
        users.save(u);
    }

    private void onFailure(UUID userId, UUID tenantId) {
        inUserScope(tenantId, () -> {
            User u = users.findById(userId).orElseThrow();
            int failed = (u.getFailedLoginAttempts() == null ? 0 : u.getFailedLoginAttempts()) + 1;
            u.setFailedLoginAttempts(failed);
            u.setLastFailedLogin(OffsetDateTime.now());
            if (failed >= MAX_FAILED_ATTEMPTS) {
                u.setAccountLockedUntil(OffsetDateTime.now().plusMinutes(LOCK_MINUTES));
                u.setFailedLoginAttempts(0);
            }
            return users.save(u);
        });
    }

    private void record(UUID userId, UUID tenantId, String login, InetAddress ip, LoginStatus status, String reason) {
        inUserScope(tenantId, () -> {
            LoginHistory h = new LoginHistory();
            h.setUserId(userId);
            h.setTenantId(tenantId);
            h.setUsername(login.length() > 100 ? login.substring(0, 100) : login);
            h.setIpAddress(ip);
            h.setLoginStatus(status);
            h.setLoginMethod(LoginMethod.PASSWORD);
            h.setFailureReason(reason);
            return loginHistory.save(h);
        });
    }

    private <T> T inUserScope(UUID tenantId, java.util.function.Supplier<T> work) {
        return tenantId == null ? sys.platform(work) : sys.inTenant(tenantId, work);
    }

    /** Builds a tenant-scoped token for an active support-access session (used by PlatformService). */
    @Override
    public String supportToken(UUID platformUserId, UUID tenantId, UUID supportAccessId) {
        String platformRole = sys.platform(() -> platformProfiles.findByUserId(platformUserId)
                .map(PlatformUserProfile::getPlatformRole).map(Enum::name).orElse(null));
        return tokens.issue(platformUserId, tenantId, UserType.PLATFORM.name(), true, platformRole, List.of(), supportAccessId);
    }

    @Override
    public long ttlSeconds() { return tokens.ttlSeconds(); }
}
