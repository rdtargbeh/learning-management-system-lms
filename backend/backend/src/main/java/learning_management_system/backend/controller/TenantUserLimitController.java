package learning_management_system.backend.controller;

import learning_management_system.backend.dto.TenantUserLimitDto;
import learning_management_system.backend.dto.UpdateTenantUserLimitRequest;
import learning_management_system.backend.service.TenantUserLimitService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** tenant_user_limit: the institution reads its limits; platform admins read and set any institution's limits. */
@RestController
@RequestMapping("/api/v1")
public class TenantUserLimitController {
    private final TenantUserLimitService limits;

    public TenantUserLimitController(TenantUserLimitService limits) { this.limits = limits; }

    @GetMapping("/tenant/user-limits")
    public TenantUserLimitDto mine() { return limits.get(null); }

    @GetMapping("/platform/tenants/{tenantId}/user-limits")
    public TenantUserLimitDto get(@PathVariable UUID tenantId) { return limits.get(tenantId); }

    @PutMapping("/platform/tenants/{tenantId}/user-limits")
    public TenantUserLimitDto update(@PathVariable UUID tenantId, @Valid @RequestBody UpdateTenantUserLimitRequest r) {
        return limits.update(tenantId, r);
    }
}
