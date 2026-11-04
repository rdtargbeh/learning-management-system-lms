package learning_management_system.backend.dto;

import java.util.UUID;
import learning_management_system.backend.enums.GroupMemberRole;

/** Add a student to a group. Students omit userId to join a self-signup group themselves. */
public record AddGroupMemberRequest(UUID userId, GroupMemberRole role) {}
