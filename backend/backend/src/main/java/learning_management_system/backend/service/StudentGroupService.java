package learning_management_system.backend.service;

import learning_management_system.backend.dto.CreateStudentGroupRequest;
import learning_management_system.backend.dto.StudentGroupDto;
import learning_management_system.backend.dto.UpdateStudentGroupRequest;

import java.util.List;
import java.util.UUID;

/** student_group: the groups of a group set. The course's teachers manage them; course viewers see them. */
public interface StudentGroupService {

    List<StudentGroupDto> list(UUID groupSetId);

    StudentGroupDto get(UUID groupId);

    StudentGroupDto create(UUID groupSetId, CreateStudentGroupRequest r);

    /** PATCH. A lower maxMembers than the current members is refused. */
    StudentGroupDto update(UUID groupId, UpdateStudentGroupRequest r);

    /** Refused once group submissions exist (409 GROUP_HAS_SUBMISSIONS): deactivate the group instead. */
    void delete(UUID groupId);
}
