package learning_management_system.backend.controller;

import learning_management_system.backend.dto.TenantSettingsDto;
import learning_management_system.backend.service.TenantSettingsService;

import org.springframework.web.bind.annotation.*;

/** tenant_settings of the caller's institution. */
@RestController
@RequestMapping("/api/v1/tenant/settings")
public class TenantSettingsController {
    private final TenantSettingsService settings;

    public TenantSettingsController(TenantSettingsService settings) { this.settings = settings; }

    @GetMapping
    public TenantSettingsDto get() { return settings.get(); }

    @PatchMapping
    public TenantSettingsDto update(@RequestBody TenantSettingsDto d) { return settings.update(d); }
}
