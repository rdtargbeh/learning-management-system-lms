package learning_management_system.backend.security;

import learning_management_system.backend.exception.ApiException;

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
        RequestContext c = ctx();
        boolean ok = "TENANT_ADMIN".equals(c.userType()) || c.supportAccessId() != null || c.permissions().contains(permission);
        if (!ok) throw ApiException.forbidden("Missing permission " + permission);
    }

    public static boolean isSelf(java.util.UUID userId) {
        return userId != null && userId.equals(ctx().userId());
    }
}
