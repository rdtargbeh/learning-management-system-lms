package learning_management_system.backend.service;

import learning_management_system.backend.dto.AppointGroupLeaderRequest;
import learning_management_system.backend.dto.GroupLeaderDto;

import java.util.List;
import java.util.UUID;

/**
 * group_leader: who leads a group and in which role. The course's teachers appoint and end leaders; a set with
 * auto_assign_leader makes the first member the PROJECT_LEAD and hands the role on when that leader leaves.
 */
public interface GroupLeaderService {

    /** Current and past leaders (course viewers). */
    List<GroupLeaderDto> list(UUID groupId);

    GroupLeaderDto appoint(UUID groupId, AppointGroupLeaderRequest r);

    /** Ends the leadership now; the member stays in the group. */
    GroupLeaderDto end(UUID groupId, UUID leaderId);

    /** Internal: a member joined (auto-appoints the first member when the set asks for it). */
    void memberJoined(UUID groupId, UUID userId, boolean autoAssign);

    /** Internal: a member left (ends their leaderships; hands on the lead when the set auto-assigns). */
    void memberLeft(UUID groupId, UUID userId, boolean autoAssign);
}
