package learning_management_system.backend.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import learning_management_system.backend.enums.VideoProvider;

/** Video details of a VIDEO item (table video_lecture). As a request, videoUrl is required. */
public record VideoLectureDto(@Size(max = 500) String videoUrl, VideoProvider videoProvider, @Positive Integer videoDurationSeconds,
                              @Size(max = 500) String thumbnailUrl, String transcript, @Size(max = 500) String transcriptUrl,
                              @Size(max = 500) String captionsUrl, Boolean captionsAvailable) {}
