package learning_management_system.backend.security;

import learning_management_system.backend.exception.ApiException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

/** Authorization checks shared by services (the database separately enforces tenant isolation). */
public final class Access {
    private Access() {}

    public static RequestContext ctx() { return RequestContext.current(); }

    public static void requirePlatformAdmin() {
        if (!ctx().platformAdmin()) throw ApiException.forbidden("Platform administrator access required");
    }

    public static void requirePlatformUser() {
        if (!ctx().isPlatformUser()) throw ApiException.forbidden("Platform user access required");
    }

    public static void requireTenant() {
        if (ctx().tenantId() == null) throw ApiException.forbidden("This action needs an institution (tenant) context");
    }

    /** Institution admin, support session, or an explicit permission. */
    public static void requireTenantPermission(String permission) {
        requireTenant();
        if (!hasTenantPermission(permission)) throw ApiException.forbidden("Missing permission " + permission);
    }

    /** True for an institution admin, a support session, or a caller whose roles grant one of the permissions. */
    public static boolean hasTenantPermission(String... permissions) {
        RequestContext c = ctx();
        if (c.tenantId() == null) return false;
        if ("TENANT_ADMIN".equals(c.userType()) || c.supportAccessId() != null) return true;
        for (String p : permissions) if (c.permissions().contains(p)) return true;
        return false;
    }

    /** The user_session of the current access token, or null (support sessions, internal work). */
    public static UUID currentSessionId() {
        if (SecurityContextHolder.getContext().getAuthentication()
                instanceof JwtAuthenticationToken t) {
            String sid = t.getToken().getClaimAsString(TokenService.CLAIM_SESSION);
            return sid == null ? null : UUID.fromString(sid);
        }
        return null;
    }

    public static boolean isSelf(UUID userId) {
        return userId != null && userId.equals(ctx().userId());
    }
}
