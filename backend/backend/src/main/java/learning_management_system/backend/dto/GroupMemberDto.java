package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.GroupMemberRole;

/** A member of a group (table group_members). */
public record GroupMemberDto(UUID memberId, UUID groupId, UUID userId, String username, String displayName, GroupMemberRole role,
                             OffsetDateTime joinedAt, OffsetDateTime leftAt, Boolean isActive) {}
