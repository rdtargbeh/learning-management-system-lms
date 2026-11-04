package learning_management_system.backend.service;

import learning_management_system.backend.dto.AddGroupMemberRequest;
import learning_management_system.backend.dto.GroupMemberDto;
import learning_management_system.backend.dto.MyGroupDto;
import learning_management_system.backend.entity.GroupSet;
import learning_management_system.backend.entity.StudentGroup;

import java.util.List;
import java.util.UUID;

/**
 * group_members: who is in a group. Teachers add and remove students; with self sign-up students join and leave
 * themselves. A student is in at most one group per set, and only enrolled students (of the group's section when the
 * set is restricted) can join.
 */
public interface GroupMemberService {

    List<GroupMemberDto> list(UUID groupId);

    /** userId omitted = the caller joins. */
    GroupMemberDto add(UUID groupId, AddGroupMemberRequest r);

    void remove(UUID groupId, UUID userId);

    /** The caller's groups in a course. */
    List<MyGroupDto> mine(UUID courseId);

    /** Internal (no permission check): adds an eligible student to a group locked by the caller. */
    void join(StudentGroup lockedGroup, GroupSet set, UUID userId);

    /** Internal: the students who may be in the set's groups (all of the course, or the given section). */
    boolean isEligible(GroupSet set, UUID sectionId, UUID userId);
}
