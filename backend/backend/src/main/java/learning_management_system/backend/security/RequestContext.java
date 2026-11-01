package learning_management_system.backend.security;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Who is calling and in which tenant. Every database transaction copies these values into
 * PostgreSQL session settings (app.current_tenant_id, app.current_user_id, app.is_platform_admin),
 * which the row-level security policies use.
 */
public record RequestContext(UUID tenantId, UUID userId, boolean platformAdmin, String userType,
                             UUID supportAccessId, Set<String> permissions) {

    private static final ThreadLocal<RequestContext> HOLDER = new ThreadLocal<>();
    private static final RequestContext ANONYMOUS = new RequestContext(null, null, false, null, null, Set.of());

    public static RequestContext current() {
        RequestContext c = HOLDER.get();
        return c == null ? ANONYMOUS : c;
    }

    public static void set(RequestContext context) { HOLDER.set(context); }

    public static void clear() { HOLDER.remove(); }

    public boolean isPlatformUser() { return "PLATFORM".equals(userType); }

    /** Runs code with a different context (for example sign-in lookups or platform bootstrap), then restores the previous one. */
    public static <T> T runAs(RequestContext context, Supplier<T> work) {
        RequestContext previous = HOLDER.get();
        HOLDER.set(context);
        try {
            return work.get();
        } finally {
            if (previous == null) HOLDER.remove(); else HOLDER.set(previous);
        }
    }

    /** Platform-level system context: sees platform rows; used only by trusted server code (sign-in, bootstrap). */
    public static RequestContext system() {
        return new RequestContext(null, null, true, "SYSTEM", null, Set.of());
    }

    /** System context scoped to one tenant (e.g. resolving a tenant user at sign-in). */
    public static RequestContext systemInTenant(UUID tenantId) {
        return new RequestContext(tenantId, null, true, "SYSTEM", null, Set.of());
    }
}
