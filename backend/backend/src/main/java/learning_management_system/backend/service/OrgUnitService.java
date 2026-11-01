package learning_management_system.backend.service;

import learning_management_system.backend.dto.CreateOrgUnitRequest;
import learning_management_system.backend.dto.OrgUnitDto;
import learning_management_system.backend.dto.UpdateOrgUnitRequest;

import java.util.List;
import java.util.UUID;

/** The institution hierarchy (campus, college, school, department, program). Everyone in the tenant can read it. */
public interface OrgUnitService {

    List<OrgUnitDto> list();

    OrgUnitDto create(CreateOrgUnitRequest r);

    OrgUnitDto update(UUID orgUnitId, UpdateOrgUnitRequest r);
}
