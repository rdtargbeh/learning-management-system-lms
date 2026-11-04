package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CreateOrgUnitRequest;
import learning_management_system.backend.dto.OrgUnitDto;
import learning_management_system.backend.dto.UpdateOrgUnitRequest;
import learning_management_system.backend.entity.OrgUnit;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.OrgUnitMapper;
import learning_management_system.backend.repository.OrgUnitRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.OrgUnitService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OrgUnitServiceImplementation implements OrgUnitService {
    private final OrgUnitRepository orgUnits;
    private final OrgUnitMapper orgUnitMapper;
    private final EntityRefresher refresher;

    public OrgUnitServiceImplementation(OrgUnitRepository orgUnits, OrgUnitMapper orgUnitMapper, EntityRefresher refresher) {
        this.orgUnits = orgUnits;
        this.orgUnitMapper = orgUnitMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrgUnitDto> list() {
        Access.requireTenant();
        return orgUnits.findAllByOrderByUnitNameAsc().stream().map(orgUnitMapper::toDto).toList();
    }

    @Transactional
    @Override
    public OrgUnitDto create(CreateOrgUnitRequest r) {
        Access.requireTenantPermission(Permissions.ACADEMIC_STRUCTURE_MANAGE);
        if (r.parentOrgUnitId() != null) load(r.parentOrgUnitId());
        OrgUnit u = orgUnitMapper.toEntity(r);
        u.setUnitName(r.unitName().trim());
        return orgUnitMapper.toDto(refresher.refresh(orgUnits.saveAndFlush(u)));
    }

    @Transactional
    @Override
    public OrgUnitDto update(UUID orgUnitId, UpdateOrgUnitRequest r) {
        Access.requireTenantPermission(Permissions.ACADEMIC_STRUCTURE_MANAGE);
        OrgUnit u = load(orgUnitId);
        if (r.parentOrgUnitId() != null) requireNoCycle(orgUnitId, r.parentOrgUnitId());
        orgUnitMapper.updateEntity(r, u);
        return orgUnitMapper.toDto(orgUnits.saveAndFlush(u));
    }

    /** The new parent must exist and must not be the unit itself or one of its descendants. */
    private void requireNoCycle(UUID unitId, UUID newParentId) {
        Set<UUID> seen = new HashSet<>();
        for (UUID id = newParentId; id != null; id = load(id).getParentOrgUnitId()) {
            if (id.equals(unitId) || !seen.add(id)) {
                throw ApiException.badRequest("ORG_UNIT_CYCLE", "An org unit cannot be placed under itself or one of its children");
            }
        }
    }

    private OrgUnit load(UUID id) {
        return orgUnits.findById(id).orElseThrow(() -> ApiException.notFound("Org unit"));
    }
}
