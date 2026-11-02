package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.SessionDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.Tenant;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.entity.UserRole;
import learning_management_system.backend.entity.UserSession;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.enums.TenantStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.IdentityMapper;
import learning_management_system.backend.mapper.UserMapper;
import learning_management_system.backend.repository.PlatformUserProfileRepository;
import learning_management_system.backend.repository.RolePermissionRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.UserProfileRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.repository.UserRoleRepository;
import learning_management_system.backend.repository.UserSessionRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.security.TokenService;
import learning_management_system.backend.service.AuditService;
import learning_management_system.backend.service.SessionService;
import learning_management_system.backend.utility.Tokens;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class SessionServiceImplementation implements SessionService {
    /** How long the filter trusts a positive "session active" answer before asking the database again. */
    private static final long ACTIVE_CACHE_MILLIS = 5_000;

    private final UserSessionRepository sessions;
    private final UserRepository users;
    private final TenantRepository tenants;
    private final UserRoleRepository userRoles;
    private final RolePermissionRepository rolePermissions;
    private final PlatformUserProfileRepository platformProfiles;
    private final UserProfileRepository profiles;
    private final UserMapper userMapper;
    private final AuditService audit;
    private final TokenService tokens;
    private final IdentityMapper identityMapper;
    private final SystemTx sys;
    private final int refreshTtlDays;
    private final Map<UUID, Long> activeCache = new ConcurrentHashMap<>();

    public SessionServiceImplementation(UserSessionRepository sessions, UserRepository users, TenantRepository tenants,
                                        UserRoleRepository userRoles, RolePermissionRepository rolePermissions,
                                        PlatformUserProfileRepository platformProfiles, UserProfileRepository profiles,
                                        UserMapper userMapper, AuditService audit,
                                        TokenService tokens, IdentityMapper identityMapper, SystemTx sys,
                                        @Value("${lms.security.refresh-ttl-days:30}") int refreshTtlDays) {
        this.sessions = sessions;
        this.users = users;
        this.tenants = tenants;
        this.userRoles = userRoles;
        this.rolePermissions = rolePermissions;
        this.platformProfiles = platformProfiles;
        this.profiles = profiles;
        this.userMapper = userMapper;
        this.audit = audit;
        this.tokens = tokens;
        this.identityMapper = identityMapper;
        this.sys = sys;
        this.refreshTtlDays = refreshTtlDays;
    }

    @Override
    public LoginResponse start(User u, UUID tenantId, InetAddress ip, String userAgent, boolean mfaSetupRequired) {
        String refresh = Tokens.random();
        UserSession s = new UserSession();
        s.setUserId(u.getUserId());
        s.setTenantId(tenantId);
        s.setTokenHash(Tokens.sha256("session:" + refresh));
        s.setRefreshTokenHash(Tokens.sha256(refresh));
        s.setIpAddress(ip);
        s.setUserAgent(trim(userAgent));
        s.setDeviceType(deviceType(userAgent));
        OffsetDateTime now = OffsetDateTime.now();
        s.setLastActivity(now);
        s.setDateExpired(now.plusDays(refreshTtlDays));
        s.setIsActive(true);
        sessions.saveAndFlush(s);
        return response(u, tenantId, s, refresh, mfaSetupRequired);
    }

    private LoginResponse response(User u, UUID tenantId, UserSession s, String refresh, boolean mfaSetupRequired) {
        List<String> perms = permissionsOf(u.getUserId());
        boolean system = Boolean.TRUE.equals(u.getIsSystemUser());
        String platformRole = system ? platformProfiles.findByUserId(u.getUserId()).map(p -> p.getPlatformRole().name()).orElse(null) : null;
        String access = tokens.issue(u.getUserId(), tenantId, u.getUserType().name(), system, platformRole, perms, null, s.getSessionId());
        UserSummaryDto summary = userMapper.toSummaryDto(u, profiles.findByUserId(u.getUserId()).orElse(null));
        return new LoginResponse(access, "Bearer", tokens.ttlSeconds(), refresh, refreshTtlDays * 86_400L, s.getSessionId(), summary, perms,
                platformRole, null, false, null, mfaSetupRequired);
    }

    /** Runs in a trusted context: the caller only has the refresh token. The old refresh token stops working. */
    @Override
    public LoginResponse refresh(String refreshToken, InetAddress ip, String userAgent) {
        UserSession found = sys.platform(() -> sessions.findByRefreshTokenHash(Tokens.sha256(refreshToken)).orElse(null));
        if (found == null || !Boolean.TRUE.equals(found.getIsActive()) || found.getRevokedAt() != null
                || found.getDateExpired().isBefore(OffsetDateTime.now())) {
            throw ApiException.unauthorized("Sign in again");
        }
        UUID tenantId = found.getTenantId();
        return scope(tenantId, () -> {
            UserSession s = sessions.findById(found.getSessionId()).orElseThrow();
            User u = users.findById(s.getUserId()).orElseThrow(() -> ApiException.unauthorized("Sign in again"));
            if (u.getAccountStatus() != AccountStatus.ACTIVE) {
                end(s);
                throw ApiException.unauthorized("Account is not active");
            }
            if (tenantId != null) {
                Tenant t = tenants.findById(tenantId).orElse(null);
                if (t == null || t.getTenantStatus() != TenantStatus.ACTIVE || !Boolean.TRUE.equals(t.getIsActive())) {
                    end(s);
                    throw ApiException.unauthorized("Institution is not active");
                }
            }
            String refresh = Tokens.random();
            s.setRefreshTokenHash(Tokens.sha256(refresh));
            s.setLastActivity(OffsetDateTime.now());
            if (ip != null) s.setIpAddress(ip);
            sessions.saveAndFlush(s);
            return response(u, tenantId, s, refresh, false);
        });
    }

    @Transactional
    @Override
    public void logout() {
        UUID sid = Access.currentSessionId();
        if (sid == null) return;                         // support-session tokens end with the support session
        sessions.findById(sid).ifPresent(this::end);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SessionDto> mine() {
        UUID sid = Access.currentSessionId();
        OffsetDateTime now = OffsetDateTime.now();
        return sessions.findByUserIdAndIsActiveTrueOrderByLastActivityDesc(Access.ctx().userId()).stream()
                .filter(s -> s.getDateExpired().isAfter(now))
                .map(s -> identityMapper.toDto(s, s.getSessionId().equals(sid))).toList();
    }

    @Transactional
    @Override
    public void revoke(UUID sessionId) {
        UserSession s = sessions.findById(sessionId).orElseThrow(() -> ApiException.notFound("Session"));
        if (!Access.isSelf(s.getUserId())) {
            Access.requireTenantPermission(Permissions.USER_MANAGE);
            audit.record("SESSION_REVOKED", "USER_SESSION", sessionId, s.getUserId(), Severity.WARNING, null, null);
        }
        end(s);
    }

    @Override
    public void revokeAll(UUID userId) {
        sessions.revokeAllForUser(userId, OffsetDateTime.now());
        activeCache.clear();
    }

    @Override
    public List<String> permissionsOf(UUID userId) {
        List<UUID> roleIds = userRoles.findByUserId(userId).stream().map(UserRole::getRoleId).toList();
        return roleIds.isEmpty() ? List.of() : rolePermissions.findPermissionCodesForRoles(roleIds).stream().distinct().sorted().toList();
    }

    @Override
    public boolean isActive(UUID sessionId) {
        Long checked = activeCache.get(sessionId);
        long now = System.currentTimeMillis();
        if (checked != null && now - checked < ACTIVE_CACHE_MILLIS) return true;
        boolean active = sys.platform(() -> sessions.findById(sessionId)
                .map(s -> Boolean.TRUE.equals(s.getIsActive()) && s.getRevokedAt() == null && s.getDateExpired().isAfter(OffsetDateTime.now()))
                .orElse(false));
        if (active) activeCache.put(sessionId, now); else activeCache.remove(sessionId);
        return active;
    }

    // ---------------------------------------------------------------- helpers

    private void end(UserSession s) {
        s.setIsActive(false);
        s.setRevokedAt(OffsetDateTime.now());
        sessions.saveAndFlush(s);
        activeCache.remove(s.getSessionId());
    }

    private <T> T scope(UUID tenantId, Supplier<T> work) {
        return tenantId == null ? sys.platform(work) : sys.inTenant(tenantId, work);
    }

    private static String trim(String s) {
        return s == null ? null : s.length() > 1000 ? s.substring(0, 1000) : s;
    }

    private static String deviceType(String ua) {
        if (ua == null) return null;
        String u = ua.toLowerCase();
        if (u.contains("ipad") || u.contains("tablet")) return "TABLET";
        if (u.contains("mobile") || u.contains("android") || u.contains("iphone")) return "MOBILE";
        return "DESKTOP";
    }
}
