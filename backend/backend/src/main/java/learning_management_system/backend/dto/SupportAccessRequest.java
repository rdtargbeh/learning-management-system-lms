package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** A platform user asks for time-boxed access to one institution. */
public record SupportAccessRequest(@NotNull UUID tenantId, @NotBlank String accessReason, String ticketReference,
                                   Integer durationMinutes, UUID impersonatedUserId) {}
