package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.EnrollmentDto;
import learning_management_system.backend.dto.WaitlistDto;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.entity.Waitlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/** enrollment and waitlist. */
@Mapper(config = MapStructConfig.class)
public interface EnrollmentMapper {

    @Mapping(target = "courseId", source = "courseId")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "displayName", source = "displayName")
    EnrollmentDto toDto(Enrollment enrollment, UUID courseId, String username, String displayName);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "displayName", source = "displayName")
    WaitlistDto toDto(Waitlist waitlist, String username, String displayName);
}
