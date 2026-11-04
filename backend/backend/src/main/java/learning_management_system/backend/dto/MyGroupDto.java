package learning_management_system.backend.dto;

import learning_management_system.backend.enums.GroupMemberRole;

import java.util.UUID;

/** A group the signed-in student belongs to in a course. */
public record MyGroupDto(UUID groupId, String groupName, UUID groupSetId, String setName, GroupMemberRole role, Integer currentMembers) {}
