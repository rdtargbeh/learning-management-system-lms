package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/** Lock an account. lockedUntil = null locks it until an administrator unlocks it. */
public record LockAccountRequest(@NotBlank @Size(max = 255) String reason, OffsetDateTime lockedUntil) {}
