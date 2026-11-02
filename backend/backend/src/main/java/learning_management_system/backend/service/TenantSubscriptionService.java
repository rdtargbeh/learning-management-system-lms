package learning_management_system.backend.service;

import learning_management_system.backend.dto.TenantSubscriptionDto;
import learning_management_system.backend.dto.UpsertTenantSubscriptionRequest;
import learning_management_system.backend.entity.TenantSubscription;

import java.util.Optional;
import java.util.UUID;

/** tenant_subscription: an institution's plan. Platform admins set it; institution admins can read it. */
public interface TenantSubscriptionService {

    /** The plan of an institution (platform admins), or of the caller's own institution when tenantId is null. */
    TenantSubscriptionDto get(UUID tenantId);

    /** Creates or replaces the plan (platform admins). */
    TenantSubscriptionDto upsert(UUID tenantId, UpsertTenantSubscriptionRequest r);

    /** The plan in force today (active, started and not ended), for limit checks. Runs in the caller's transaction. */
    Optional<TenantSubscription> current(UUID tenantId);
}
