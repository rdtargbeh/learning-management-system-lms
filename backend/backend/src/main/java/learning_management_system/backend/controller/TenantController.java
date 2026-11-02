package learning_management_system.backend.controller;

import learning_management_system.backend.dto.TenantBrandingDto;
import learning_management_system.backend.dto.TenantDto;
import learning_management_system.backend.dto.TenantSettingsDto;
import learning_management_system.backend.dto.TenantSubscriptionDto;
import learning_management_system.backend.dto.TenantUserLimitDto;
import learning_management_system.backend.service.TenantBrandingService;
import learning_management_system.backend.service.TenantPlanService;
import learning_management_system.backend.service.TenantService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** The caller's own institution. */
@RestController
@RequestMapping("/api/v1/tenant")
public class TenantController {
    private final TenantService tenants;
    private final TenantBrandingService branding;
    private final TenantPlanService plans;

    public TenantController(TenantService tenants, TenantBrandingService branding, TenantPlanService plans) {
        this.tenants = tenants;
        this.branding = branding;
        this.plans = plans;
    }

    @GetMapping
    public TenantDto current() { return tenants.current(); }

    @GetMapping("/settings")
    public TenantSettingsDto settings() { return tenants.settings(); }

    @GetMapping("/branding")
    public TenantBrandingDto branding() { return branding.get(); }

    @PutMapping("/branding")
    public TenantBrandingDto replaceBranding(@Valid @RequestBody TenantBrandingDto d) { return branding.replace(d); }

    @GetMapping("/subscription")
    public TenantSubscriptionDto subscription() { return plans.subscription(null); }

    @GetMapping("/user-limits")
    public TenantUserLimitDto userLimits() { return plans.userLimits(null); }

    @PatchMapping("/settings")
    public TenantSettingsDto updateSettings(@RequestBody TenantSettingsDto d) { return tenants.updateSettings(d); }
}
