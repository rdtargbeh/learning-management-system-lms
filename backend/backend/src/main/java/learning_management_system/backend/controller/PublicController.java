package learning_management_system.backend.controller;

import learning_management_system.backend.dto.PublicBrandingDto;
import learning_management_system.backend.service.TenantBrandingService;

import org.springframework.web.bind.annotation.*;

/** Endpoints that work without signing in (sign-in page support). */
@RestController
@RequestMapping("/api/v1/public")
public class PublicController {
    private final TenantBrandingService branding;

    public PublicController(TenantBrandingService branding) { this.branding = branding; }

    /** Logo and colors for an institution's sign-in page. */
    @GetMapping("/tenants/{tenantCode}/branding")
    public PublicBrandingDto branding(@PathVariable String tenantCode) { return branding.publicBranding(tenantCode); }
}
