package learning_management_system.backend.service;

import learning_management_system.backend.dto.VideoLectureDto;
import learning_management_system.backend.entity.Content;

import java.util.UUID;

/** video_lecture: the video details of a content item (URL, provider, captions, transcript). */
public interface VideoLectureService {

    /** Anyone who may open the content item (404 when it has no video). */
    VideoLectureDto get(UUID contentId);

    /** Creates or updates the video (course editors). videoUrl is required for a new video. */
    VideoLectureDto save(UUID contentId, VideoLectureDto d);

    void delete(UUID contentId);

    /** Internal: creates or updates the video of an item the caller may already edit. */
    void saveFor(Content content, VideoLectureDto d);
}
