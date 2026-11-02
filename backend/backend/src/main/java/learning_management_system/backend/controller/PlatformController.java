package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CreatePlatformUserRequest;
import learning_management_system.backend.dto.CreateTenantRequest;
import learning_management_system.backend.dto.CreateTenantResponse;
import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.PlatformSupportAccessDto;
import learning_management_system.backend.dto.PlatformUserProfileDto;
import learning_management_system.backend.dto.SupportAccessRequest;
import learning_management_system.backend.dto.TenantDto;
import learning_management_system.backend.dto.TenantSubscriptionDto;
import learning_management_system.backend.dto.TenantUserLimitDto;
import learning_management_system.backend.dto.UpdateTenantUserLimitRequest;
import learning_management_system.backend.dto.UpsertTenantSubscriptionRequest;
import learning_management_system.backend.dto.UpdateTenantRequest;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.service.PlatformService;
import learning_management_system.backend.service.TenantPlanService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Platform operator API (platform users only). */
@RestController
@RequestMapping("/api/v1/platform")
public class PlatformController {
    private final PlatformService platform;
    private final TenantPlanService plans;

    public PlatformController(PlatformService platform, TenantPlanService plans) {
        this.platform = platform;
        this.plans = plans;
    }

    @PostMapping("/tenants")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTenantResponse createTenant(@Valid @RequestBody CreateTenantRequest r) { return platform.createTenant(r); }

    @GetMapping("/tenants")
    public PageResponse<TenantDto> tenants(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return platform.listTenants(page, size);
    }

    @GetMapping("/tenants/{tenantId}")
    public TenantDto tenant(@PathVariable UUID tenantId) { return platform.getTenant(tenantId); }

    @PatchMapping("/tenants/{tenantId}")
    public TenantDto updateTenant(@PathVariable UUID tenantId, @RequestBody UpdateTenantRequest r) { return platform.updateTenant(tenantId, r); }

    @GetMapping("/tenants/{tenantId}/subscription")
    public TenantSubscriptionDto subscription(@PathVariable UUID tenantId) { return plans.subscription(tenantId); }

    @PutMapping("/tenants/{tenantId}/subscription")
    public TenantSubscriptionDto upsertSubscription(@PathVariable UUID tenantId, @Valid @RequestBody UpsertTenantSubscriptionRequest r) {
        return plans.upsertSubscription(tenantId, r);
    }

    @GetMapping("/tenants/{tenantId}/user-limits")
    public TenantUserLimitDto userLimits(@PathVariable UUID tenantId) { return plans.userLimits(tenantId); }

    @PutMapping("/tenants/{tenantId}/user-limits")
    public TenantUserLimitDto updateUserLimits(@PathVariable UUID tenantId, @Valid @RequestBody UpdateTenantUserLimitRequest r) {
        return plans.updateUserLimits(tenantId, r);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDetailDto createPlatformUser(@Valid @RequestBody CreatePlatformUserRequest r) { return platform.createPlatformUser(r); }

    @GetMapping("/users")
    public List<UserSummaryDto> platformUsers() { return platform.listPlatformUsers(); }

    @PatchMapping("/users/{userId}/platform-profile")
    public PlatformUserProfileDto updatePlatformProfile(@PathVariable UUID userId, @RequestBody PlatformUserProfileDto d) {
        return platform.updatePlatformProfile(userId, d);
    }

    @PostMapping("/support-access")
    @ResponseStatus(HttpStatus.CREATED)
    public PlatformSupportAccessDto requestSupport(@Valid @RequestBody SupportAccessRequest r) { return platform.requestSupportAccess(r); }

    @GetMapping("/support-access")
    public List<PlatformSupportAccessDto> supportAccess() { return platform.mySupportAccess(); }

    @PostMapping("/support-access/{id}/approve")
    public PlatformSupportAccessDto approve(@PathVariable UUID id) { return platform.approveSupportAccess(id, true); }

    @PostMapping("/support-access/{id}/deny")
    public PlatformSupportAccessDto deny(@PathVariable UUID id) { return platform.approveSupportAccess(id, false); }

    /** Returns a tenant-scoped token for the approved session. */
    @PostMapping("/support-access/{id}/start")
    public LoginResponse start(@PathVariable UUID id) { return platform.startSupportSession(id); }

    @PostMapping("/support-access/{id}/end")
    public PlatformSupportAccessDto end(@PathVariable UUID id) { return platform.endSupportSession(id); }
}
