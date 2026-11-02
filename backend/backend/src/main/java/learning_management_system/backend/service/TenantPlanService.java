package learning_management_system.backend.service;

import learning_management_system.backend.dto.TenantSubscriptionDto;
import learning_management_system.backend.dto.TenantUserLimitDto;
import learning_management_system.backend.dto.UpdateTenantUserLimitRequest;
import learning_management_system.backend.dto.UpsertTenantSubscriptionRequest;
import learning_management_system.backend.enums.UserType;

import java.util.UUID;

/**
 * An institution's plan (subscription) and account limits. Platform staff set them; institution admins can see them.
 * Limits are enforced when accounts are created or reactivated: students, instructors (incl. TAs), staff (incl.
 * advisors) and admins are counted separately; observers are not limited. An explicit user limit wins over the plan.
 */
public interface TenantPlanService {

    /** The plan of an institution (platform staff), or of the caller's own institution when tenantId is null. */
    TenantSubscriptionDto subscription(UUID tenantId);

    TenantSubscriptionDto upsertSubscription(UUID tenantId, UpsertTenantSubscriptionRequest r);

    /** Limits with current counts (platform staff), or the caller's own institution when tenantId is null. */
    TenantUserLimitDto userLimits(UUID tenantId);

    TenantUserLimitDto updateUserLimits(UUID tenantId, UpdateTenantUserLimitRequest r);

    /** Throws 409 USER_LIMIT_REACHED when one more account of this type would exceed the institution's limit. */
    void requireRoom(UUID tenantId, UserType type);

    /** Recounts the accounts in use. */
    void refreshCounts(UUID tenantId);
}
