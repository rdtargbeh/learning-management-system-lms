package learning_management_system.backend.service;

import learning_management_system.backend.dto.TenantUserLimitDto;
import learning_management_system.backend.dto.UpdateTenantUserLimitRequest;
import learning_management_system.backend.enums.UserType;

import java.util.UUID;

/**
 * tenant_user_limit: account limits and current counts. Platform admins set them; institution admins can read them.
 * Students, instructors (incl. TAs), staff (incl. advisors) and admins are counted separately; observers are not
 * limited. An explicit limit wins over the plan's maximum.
 */
public interface TenantUserLimitService {

    /** Limits with fresh counts (platform admins), or the caller's own institution when tenantId is null. */
    TenantUserLimitDto get(UUID tenantId);

    TenantUserLimitDto update(UUID tenantId, UpdateTenantUserLimitRequest r);

    /** Throws 409 USER_LIMIT_REACHED when one more account of this type would exceed the limit. Locks the row. */
    void requireRoom(UUID tenantId, UserType type);

    /** Recounts the accounts in use. */
    void refreshCounts(UUID tenantId);
}
