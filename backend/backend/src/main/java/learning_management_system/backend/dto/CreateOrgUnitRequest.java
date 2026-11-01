package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import learning_management_system.backend.enums.UnitType;

/** Create an org unit; parentOrgUnitId places it in the hierarchy. */
public record CreateOrgUnitRequest(@NotBlank @Size(max = 255) String unitName, @Size(max = 50) String unitCode,
                                   @NotNull UnitType unitType, UUID parentOrgUnitId, String sisSourceId) {}
