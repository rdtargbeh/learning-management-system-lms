package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CreateTenantRequest;
import learning_management_system.backend.dto.CreateTenantResponse;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.TenantDto;
import learning_management_system.backend.dto.UpdateTenantRequest;
import learning_management_system.backend.service.TenantService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** tenant: the caller's own institution (/tenant) and, for platform admins, every institution (/platform/tenants). */
@RestController
@RequestMapping("/api/v1")
public class TenantController {
    private final TenantService tenants;

    public TenantController(TenantService tenants) { this.tenants = tenants; }

    @GetMapping("/tenant")
    public TenantDto current() { return tenants.current(); }

    @PostMapping("/platform/tenants")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTenantResponse create(@Valid @RequestBody CreateTenantRequest r) { return tenants.create(r); }

    @GetMapping("/platform/tenants")
    public PageResponse<TenantDto> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return tenants.list(page, size);
    }

    @GetMapping("/platform/tenants/{tenantId}")
    public TenantDto get(@PathVariable UUID tenantId) { return tenants.get(tenantId); }

    @PatchMapping("/platform/tenants/{tenantId}")
    public TenantDto update(@PathVariable UUID tenantId, @RequestBody UpdateTenantRequest r) { return tenants.update(tenantId, r); }
}
