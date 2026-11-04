package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.UnitType;

/** A unit of the institution hierarchy: campus, college, school, department, program (table org_unit). */
public record OrgUnitDto(UUID orgUnitId, UUID parentOrgUnitId, String unitName, String unitCode, UnitType unitType,
                         String sisSourceId, Boolean isActive, OffsetDateTime dateCreated) {}
