package learning_management_system.backend.controller;

import learning_management_system.backend.dto.TenantDto;
import learning_management_system.backend.dto.TenantSettingsDto;
import learning_management_system.backend.service.TenantService;

import org.springframework.web.bind.annotation.*;

/** The caller's own institution. */
@RestController
@RequestMapping("/api/v1/tenant")
public class TenantController {
    private final TenantService tenants;

    public TenantController(TenantService tenants) { this.tenants = tenants; }

    @GetMapping
    public TenantDto current() { return tenants.current(); }

    @GetMapping("/settings")
    public TenantSettingsDto settings() { return tenants.settings(); }

    @PatchMapping("/settings")
    public TenantSettingsDto updateSettings(@RequestBody TenantSettingsDto d) { return tenants.updateSettings(d); }
}
