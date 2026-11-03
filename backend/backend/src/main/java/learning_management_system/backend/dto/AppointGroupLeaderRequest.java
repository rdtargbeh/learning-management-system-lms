package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import learning_management_system.backend.enums.LeadershipRole;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Appoints an active member as leader (default role PROJECT_LEAD); termEndAt ends the leadership automatically. */
public record AppointGroupLeaderRequest(@NotNull UUID userId, LeadershipRole leadershipRole, OffsetDateTime termEndAt) {}
