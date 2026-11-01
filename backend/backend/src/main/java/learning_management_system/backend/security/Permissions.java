package learning_management_system.backend.security;

import java.util.List;

/** System permission codes seeded into the permissions table (tenant_id NULL = shared by every tenant). */
public final class Permissions {
    private Permissions() {}
    public static final String USER_READ = "USER_READ";
    public static final String USER_MANAGE = "USER_MANAGE";
    public static final String ROLE_MANAGE = "ROLE_MANAGE";
    public static final String OBSERVER_MANAGE = "OBSERVER_MANAGE";
    public static final String TENANT_SETTINGS_MANAGE = "TENANT_SETTINGS_MANAGE";

    public record Def(String code, String name, String resource, String action) {}

    public static final List<Def> SYSTEM = List.of(
            new Def(USER_READ, "View users", "USER", "READ"),
            new Def(USER_MANAGE, "Create and edit users", "USER", "UPDATE"),
            new Def(ROLE_MANAGE, "Manage roles and role assignments", "ROLE", "UPDATE"),
            new Def(OBSERVER_MANAGE, "Link observers to students", "USER", "UPDATE"),
            new Def(TENANT_SETTINGS_MANAGE, "Edit institution settings", "TENANT", "UPDATE"));
}
