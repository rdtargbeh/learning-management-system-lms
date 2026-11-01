package learning_management_system.backend.repository;

import learning_management_system.backend.entity.VideoLecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoLectureRepository extends JpaRepository<VideoLecture, UUID> {

    Optional<VideoLecture> findFirstByContentId(UUID contentId);
}
