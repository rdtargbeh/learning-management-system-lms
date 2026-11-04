package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.ObserverStudentLinkDto;
import learning_management_system.backend.entity.ObserverStudentLink;
import org.mapstruct.Mapper;

/** observer_student_link. */
@Mapper(config = MapStructConfig.class)
public interface ObserverStudentLinkMapper {

    ObserverStudentLinkDto toDto(ObserverStudentLink link);
}
