package learning_management_system.backend.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;
import learning_management_system.backend.enums.UnitType;

/** PATCH an org unit; null fields are left unchanged. */
public record UpdateOrgUnitRequest(@Size(max = 255) String unitName, @Size(max = 50) String unitCode, UnitType unitType,
                                   UUID parentOrgUnitId, String sisSourceId, Boolean isActive) {}
