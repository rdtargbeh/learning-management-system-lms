package learning_management_system.backend.controller;

import learning_management_system.backend.dto.PublicBrandingDto;
import learning_management_system.backend.dto.TenantBrandingDto;
import learning_management_system.backend.service.TenantBrandingService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** tenant_branding: the institution's look; the public endpoints serve sign-in pages without a token. */
@RestController
@RequestMapping("/api/v1")
public class TenantBrandingController {
    private final TenantBrandingService branding;

    public TenantBrandingController(TenantBrandingService branding) { this.branding = branding; }

    @GetMapping("/tenant/branding")
    public TenantBrandingDto get() { return branding.get(); }

    @PutMapping("/tenant/branding")
    public TenantBrandingDto replace(@Valid @RequestBody TenantBrandingDto d) { return branding.replace(d); }

    /** Logo and colors for an institution's sign-in page, by institution code. */
    @GetMapping("/public/tenants/{tenantCode}/branding")
    public PublicBrandingDto publicBranding(@PathVariable String tenantCode) { return branding.publicBranding(tenantCode); }

    /** The same, by one of the institution's verified domains. */
    @GetMapping("/public/domains/{domainName}/branding")
    public PublicBrandingDto publicBrandingByDomain(@PathVariable String domainName) { return branding.publicBrandingByDomain(domainName); }
}
