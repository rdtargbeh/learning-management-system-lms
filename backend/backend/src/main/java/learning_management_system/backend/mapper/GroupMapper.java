package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.CreateGroupSetRequest;
import learning_management_system.backend.dto.CreateStudentGroupRequest;
import learning_management_system.backend.dto.GroupMemberDto;
import learning_management_system.backend.dto.GroupSetDto;
import learning_management_system.backend.dto.StudentGroupDto;
import learning_management_system.backend.entity.GroupMember;
import learning_management_system.backend.entity.GroupSet;
import learning_management_system.backend.entity.StudentGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** group_set, student_group and group_members. */
@Mapper(config = MapStructConfig.class)
public interface GroupMapper {

    GroupSetDto toDto(GroupSet groupSet);

    GroupSet toEntity(CreateGroupSetRequest request);

    StudentGroupDto toDto(StudentGroup group);

    StudentGroup toEntity(CreateStudentGroupRequest request);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "displayName", source = "displayName")
    GroupMemberDto toDto(GroupMember member, String username, String displayName);
}
