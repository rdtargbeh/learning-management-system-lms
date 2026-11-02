package learning_management_system.backend.controller;

import learning_management_system.backend.dto.InstitutionDto;
import learning_management_system.backend.dto.UpdateInstitutionRequest;
import learning_management_system.backend.service.InstitutionService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** institution: the caller's own institution record, and any institution's for platform admins. */
@RestController
@RequestMapping("/api/v1")
public class InstitutionController {
    private final InstitutionService institutions;

    public InstitutionController(InstitutionService institutions) { this.institutions = institutions; }

    @GetMapping("/tenant/institution")
    public InstitutionDto mine() { return institutions.get(null); }

    @PatchMapping("/tenant/institution")
    public InstitutionDto updateMine(@Valid @RequestBody UpdateInstitutionRequest r) { return institutions.update(null, r); }

    @GetMapping("/platform/tenants/{tenantId}/institution")
    public InstitutionDto get(@PathVariable UUID tenantId) { return institutions.get(tenantId); }

    @PatchMapping("/platform/tenants/{tenantId}/institution")
    public InstitutionDto update(@PathVariable UUID tenantId, @Valid @RequestBody UpdateInstitutionRequest r) { return institutions.update(tenantId, r); }
}
