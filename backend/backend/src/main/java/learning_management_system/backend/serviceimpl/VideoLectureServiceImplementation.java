package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.ContentDto;
import learning_management_system.backend.dto.VideoLectureDto;
import learning_management_system.backend.entity.Content;
import learning_management_system.backend.entity.VideoLecture;
import learning_management_system.backend.enums.BlueprintContentLockObjectType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.ContentMapper;
import learning_management_system.backend.repository.ContentRepository;
import learning_management_system.backend.repository.VideoLectureRepository;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.BlueprintContentLockService;
import learning_management_system.backend.service.ContentService;
import learning_management_system.backend.service.VideoLectureService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.UUID;

import static learning_management_system.backend.enums.BlueprintLockAspect.CONTENT;

@Service
public class VideoLectureServiceImplementation implements VideoLectureService {
    private final VideoLectureRepository videos;
    private final ContentRepository contents;
    private final ContentService contentService;
    private final BlueprintContentLockService blueprintLocks;
    private final CourseAccess access;
    private final ContentMapper contentMapper;

    public VideoLectureServiceImplementation(VideoLectureRepository videos, ContentRepository contents,
                                             @org.springframework.context.annotation.Lazy ContentService contentService,
                                             BlueprintContentLockService blueprintLocks, CourseAccess access, ContentMapper contentMapper) {
        this.videos = videos;
        this.contents = contents;
        this.contentService = contentService;
        this.blueprintLocks = blueprintLocks;
        this.access = access;
        this.contentMapper = contentMapper;
    }

    @Transactional
    @Override
    public VideoLectureDto get(UUID contentId) {
        ContentDto view = contentService.get(contentId);           // visibility, locks and view tracking
        if (view.video() == null) throw ApiException.notFound("Video");
        return view.video();
    }

    @Transactional
    @Override
    public VideoLectureDto save(UUID contentId, VideoLectureDto d) {
        Content x = editable(contentId);
        saveFor(x, d);
        return videos.findFirstByContentId(contentId).map(contentMapper::toDto).orElseThrow();
    }

    @Transactional
    @Override
    public void delete(UUID contentId) {
        Content x = editable(contentId);
        videos.delete(videos.findFirstByContentId(x.getContentId()).orElseThrow(() -> ApiException.notFound("Video")));
    }

    @Override
    public void saveFor(Content x, VideoLectureDto d) {
        VideoLecture v = videos.findFirstByContentId(x.getContentId()).orElse(null);
        if (v == null) {
            if (d.videoUrl() == null || d.videoUrl().isBlank()) throw ApiException.badRequest("VIDEO_URL_REQUIRED", "video.videoUrl is required");
            v = contentMapper.toEntity(d);
            v.setContentId(x.getContentId());
        } else {
            contentMapper.updateEntity(d, v);
        }
        videos.saveAndFlush(v);
    }

    private Content editable(UUID contentId) {
        Content x = contents.findById(contentId).orElseThrow(() -> ApiException.notFound("Content"));
        access.requireCourseEditor(x.getCourseId());
        blueprintLocks.requireEditable(x.getCourseId(), BlueprintContentLockObjectType.CONTENT, contentId, EnumSet.of(CONTENT));
        return x;
    }
}
