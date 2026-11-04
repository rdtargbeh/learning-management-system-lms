package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.ContentDto;
import learning_management_system.backend.dto.ContentProgressDto;
import learning_management_system.backend.dto.ContentTypeDto;
import learning_management_system.backend.dto.CreateContentRequest;
import learning_management_system.backend.dto.CreateContentTypeRequest;
import learning_management_system.backend.dto.UpdateContentRequest;
import learning_management_system.backend.dto.VideoLectureDto;
import learning_management_system.backend.entity.Content;
import learning_management_system.backend.entity.ContentType;
import learning_management_system.backend.entity.StudentContentProgress;
import learning_management_system.backend.entity.VideoLecture;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** content, content_type, video_lecture and student_content_progress. */
@Mapper(config = MapStructConfig.class)
public interface ContentMapper {

    @Mapping(target = "typeCode", source = "typeCode")
    @Mapping(target = "video", source = "video")
    @Mapping(target = "lockReason", ignore = true)
    ContentDto toDto(Content content, String typeCode, VideoLectureDto video);

    /** A student's view of an item they cannot open yet: everything but the body. */
    @Mapping(target = "typeCode", source = "typeCode")
    @Mapping(target = "lockReason", source = "lockReason")
    @Mapping(target = "contentBody", ignore = true)
    @Mapping(target = "contentHtml", ignore = true)
    @Mapping(target = "video", ignore = true)
    ContentDto toLockedDto(Content content, String typeCode, String lockReason);

    @Mapping(target = "isFrontPage", ignore = true)
    Content toEntity(CreateContentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "isFrontPage", ignore = true)
    void updateEntity(UpdateContentRequest request, @MappingTarget Content content);

    VideoLectureDto toDto(VideoLecture video);

    VideoLecture toEntity(VideoLectureDto video);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(VideoLectureDto dto, @MappingTarget VideoLecture video);

    ContentTypeDto toDto(ContentType type);

    ContentType toEntity(CreateContentTypeRequest request);

    ContentProgressDto toDto(StudentContentProgress progress);
}
