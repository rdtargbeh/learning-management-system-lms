package learning_management_system.backend.controller;

import learning_management_system.backend.dto.VideoLectureDto;
import learning_management_system.backend.service.VideoLectureService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** video_lecture: a content item's video. */
@RestController
@RequestMapping("/api/v1/content/{contentId}/video")
public class VideoLectureController {
    private final VideoLectureService videos;

    public VideoLectureController(VideoLectureService videos) { this.videos = videos; }

    @GetMapping
    public VideoLectureDto get(@PathVariable UUID contentId) { return videos.get(contentId); }

    @PutMapping
    public VideoLectureDto save(@PathVariable UUID contentId, @Valid @RequestBody VideoLectureDto d) { return videos.save(contentId, d); }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID contentId) { videos.delete(contentId); }
}
