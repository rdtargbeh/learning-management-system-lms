package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CreateOrgUnitRequest;
import learning_management_system.backend.dto.OrgUnitDto;
import learning_management_system.backend.dto.UpdateOrgUnitRequest;
import learning_management_system.backend.service.OrgUnitService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** The institution hierarchy. */
@RestController
@RequestMapping("/api/v1/org-units")
public class OrgUnitController {
    private final OrgUnitService orgUnits;

    public OrgUnitController(OrgUnitService orgUnits) { this.orgUnits = orgUnits; }

    @GetMapping
    public List<OrgUnitDto> list() { return orgUnits.list(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrgUnitDto create(@Valid @RequestBody CreateOrgUnitRequest r) { return orgUnits.create(r); }

    @PatchMapping("/{orgUnitId}")
    public OrgUnitDto update(@PathVariable UUID orgUnitId, @Valid @RequestBody UpdateOrgUnitRequest r) {
        return orgUnits.update(orgUnitId, r);
    }
}
