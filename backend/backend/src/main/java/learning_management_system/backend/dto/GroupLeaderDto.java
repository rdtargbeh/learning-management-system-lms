package learning_management_system.backend.dto;

import learning_management_system.backend.enums.LeadershipRole;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A group leader (table group_leader); current is false once the term has ended. */
public record GroupLeaderDto(UUID leaderId, UUID groupId, UUID userId, String username, String displayName, LeadershipRole leadershipRole,
                             OffsetDateTime appointedAt, UUID appointedBy, OffsetDateTime termEndAt, boolean current) {}
