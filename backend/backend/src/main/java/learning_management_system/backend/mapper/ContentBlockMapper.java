package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.ContentBlockDto;
import learning_management_system.backend.dto.ContentBlockRequest;
import learning_management_system.backend.dto.InteractiveContentDto;
import learning_management_system.backend.entity.ContentBlock;
import learning_management_system.backend.entity.InteractiveContent;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** content_block and interactive_content. */
@Mapper(config = MapStructConfig.class)
public interface ContentBlockMapper {

    ContentBlockDto toDto(ContentBlock block);

    /** PATCH: null values leave the field unchanged. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ContentBlockRequest r, @MappingTarget ContentBlock block);

    InteractiveContentDto toDto(InteractiveContent interactive);

    /** PUT: the details are replaced. */
    void replaceEntity(InteractiveContentDto d, @MappingTarget InteractiveContent interactive);
}
