package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AddTenantDomainRequest;
import learning_management_system.backend.dto.TenantDomainDto;
import learning_management_system.backend.service.TenantDomainService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** tenant_domain: the caller's institution's domains (/tenant/domains) and any institution's for platform admins. */
@RestController
@RequestMapping("/api/v1")
public class TenantDomainController {
    private final TenantDomainService domains;

    public TenantDomainController(TenantDomainService domains) { this.domains = domains; }

    // ---------------------------------------------------------------- own institution

    @GetMapping("/tenant/domains")
    public List<TenantDomainDto> mine() { return domains.list(null); }

    @PostMapping("/tenant/domains")
    @ResponseStatus(HttpStatus.CREATED)
    public TenantDomainDto add(@Valid @RequestBody AddTenantDomainRequest r) { return domains.add(null, r); }

    @PostMapping("/tenant/domains/{domainId}/verify")
    public TenantDomainDto verify(@PathVariable UUID domainId) { return domains.verify(null, domainId); }

    @PostMapping("/tenant/domains/{domainId}/primary")
    public TenantDomainDto makePrimary(@PathVariable UUID domainId) { return domains.makePrimary(null, domainId); }

    @DeleteMapping("/tenant/domains/{domainId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID domainId) { domains.remove(null, domainId); }

    // ---------------------------------------------------------------- platform admins

    @GetMapping("/platform/tenants/{tenantId}/domains")
    public List<TenantDomainDto> list(@PathVariable UUID tenantId) { return domains.list(tenantId); }

    @PostMapping("/platform/tenants/{tenantId}/domains")
    @ResponseStatus(HttpStatus.CREATED)
    public TenantDomainDto add(@PathVariable UUID tenantId, @Valid @RequestBody AddTenantDomainRequest r) { return domains.add(tenantId, r); }

    @PostMapping("/platform/tenants/{tenantId}/domains/{domainId}/verify")
    public TenantDomainDto verify(@PathVariable UUID tenantId, @PathVariable UUID domainId) { return domains.verify(tenantId, domainId); }

    @PostMapping("/platform/tenants/{tenantId}/domains/{domainId}/mark-verified")
    public TenantDomainDto markVerified(@PathVariable UUID tenantId, @PathVariable UUID domainId) { return domains.markVerified(tenantId, domainId); }

    @PostMapping("/platform/tenants/{tenantId}/domains/{domainId}/primary")
    public TenantDomainDto makePrimary(@PathVariable UUID tenantId, @PathVariable UUID domainId) { return domains.makePrimary(tenantId, domainId); }

    @DeleteMapping("/platform/tenants/{tenantId}/domains/{domainId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID tenantId, @PathVariable UUID domainId) { domains.remove(tenantId, domainId); }
}
