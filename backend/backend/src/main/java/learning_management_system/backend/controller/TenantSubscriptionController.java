package learning_management_system.backend.controller;

import learning_management_system.backend.dto.TenantSubscriptionDto;
import learning_management_system.backend.dto.UpsertTenantSubscriptionRequest;
import learning_management_system.backend.service.TenantSubscriptionService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** tenant_subscription: the institution reads its plan; platform admins read and set any institution's plan. */
@RestController
@RequestMapping("/api/v1")
public class TenantSubscriptionController {
    private final TenantSubscriptionService subscriptions;

    public TenantSubscriptionController(TenantSubscriptionService subscriptions) { this.subscriptions = subscriptions; }

    @GetMapping("/tenant/subscription")
    public TenantSubscriptionDto mine() { return subscriptions.get(null); }

    @GetMapping("/platform/tenants/{tenantId}/subscription")
    public TenantSubscriptionDto get(@PathVariable UUID tenantId) { return subscriptions.get(tenantId); }

    @PutMapping("/platform/tenants/{tenantId}/subscription")
    public TenantSubscriptionDto upsert(@PathVariable UUID tenantId, @Valid @RequestBody UpsertTenantSubscriptionRequest r) {
        return subscriptions.upsert(tenantId, r);
    }
}
