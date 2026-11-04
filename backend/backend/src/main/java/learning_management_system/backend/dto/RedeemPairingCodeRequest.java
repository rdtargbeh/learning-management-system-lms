package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import learning_management_system.backend.enums.Relationship;

/** An observer enters the student's code to follow that student (relationship defaults to PARENT). */
public record RedeemPairingCodeRequest(@NotBlank @Size(max = 20) String code, Relationship relationship) {}
