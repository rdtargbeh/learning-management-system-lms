package learning_management_system.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoleType {
    GLOBAL,      // Global roles like Super Admin
    TENANT,      // Tenant-specific roles
    SYSTEM,      // System-defined roles
    USERS,       // General users
    INSTRUCTOR,  // Teaching roles
    ADMIN,       // Administrator roles
    STUDENT,     // Student roles
    SUPPORT_STAFF;   // School staff


    @JsonCreator
    public static RoleType fromString(String value) {
        for (RoleType role : RoleType.values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid RoleType: " + value);
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}

