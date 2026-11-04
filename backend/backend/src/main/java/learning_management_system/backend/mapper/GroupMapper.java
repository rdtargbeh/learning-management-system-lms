package learning_management_system.backend.mapper;

import learning_management_system.backend.dto.UpdateGroupSetRequest;
import learning_management_system.backend.dto.UpdateStudentGroupRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
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

    /** PATCH: null values leave the field unchanged. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateGroupSetRequest request, @MappingTarget GroupSet groupSet);

    StudentGroupDto toDto(StudentGroup group);

    /** PATCH: null values leave the field unchanged. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateStudentGroupRequest request, @MappingTarget StudentGroup group);

    StudentGroup toEntity(CreateStudentGroupRequest request);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "displayName", source = "displayName")
    GroupMemberDto toDto(GroupMember member, String username, String displayName);
}
