package learning_management_system.backend.service;

import learning_management_system.backend.dto.AddGroupMemberRequest;
import learning_management_system.backend.dto.CreateGroupSetRequest;
import learning_management_system.backend.dto.CreateStudentGroupRequest;
import learning_management_system.backend.dto.GroupMemberDto;
import learning_management_system.backend.dto.GroupSetDto;
import learning_management_system.backend.dto.StudentGroupDto;

import java.util.List;
import java.util.UUID;

/**
 * Student groups, organised in group sets (Canvas group sets / Blackboard group sets). Teachers create sets and groups
 * and place students; in a self-signup set students join and leave groups themselves. A student is in at most one
 * group per set, and only students enrolled in the course (or the group's section) can join.
 */
public interface GroupService {

    List<GroupSetDto> listSets(UUID courseId);

    GroupSetDto createSet(UUID courseId, CreateGroupSetRequest r);

    List<StudentGroupDto> listGroups(UUID groupSetId);

    StudentGroupDto createGroup(UUID groupSetId, CreateStudentGroupRequest r);

    List<GroupMemberDto> members(UUID groupId);

    GroupMemberDto addMember(UUID groupId, AddGroupMemberRequest r);

    void removeMember(UUID groupId, UUID userId);
}
