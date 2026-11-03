package learning_management_system.backend.service;

import learning_management_system.backend.dto.AutoAssignGroupsRequest;
import learning_management_system.backend.dto.AutoAssignGroupsResultDto;
import learning_management_system.backend.dto.CreateGroupSetRequest;
import learning_management_system.backend.dto.GroupSetDto;
import learning_management_system.backend.dto.UpdateGroupSetRequest;

import java.util.List;
import java.util.UUID;

/** group_set: a course's ways of grouping students (project teams, lab partners ...). Managed by the course's teachers. */
public interface GroupSetService {

    List<GroupSetDto> list(UUID courseId);

    GroupSetDto get(UUID groupSetId);

    GroupSetDto create(UUID courseId, CreateGroupSetRequest r);

    GroupSetDto update(UUID groupSetId, UpdateGroupSetRequest r);

    /** Deletes the set with its groups; refused while a group assignment uses it or a group handed in work. */
    void delete(UUID groupSetId);

    /** Assigns the unassigned enrolled students at random, smallest groups first (Canvas "auto-assign"). */
    AutoAssignGroupsResultDto autoAssign(UUID groupSetId, AutoAssignGroupsRequest r);
}
