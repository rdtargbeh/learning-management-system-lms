package learning_management_system.backend.repository;

import learning_management_system.backend.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<Content, UUID> {

    List<Content> findByCourseIdOrderByDisplayOrderAscContentTitleAsc(UUID courseId);

    List<Content> findByModuleIdOrderByDisplayOrderAscContentTitleAsc(UUID moduleId);

    List<Content> findByModuleIdIn(Collection<UUID> moduleIds);

    Optional<Content> findFirstByCourseIdAndIsFrontPageTrue(UUID courseId);

    @Query("select coalesce(max(c.displayOrder), 0) from Content c where c.moduleId = :moduleId")
    int maxDisplayOrderInModule(@Param("moduleId") UUID moduleId);

    /** At most one front page per course (unique index); clear the old one first. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Content c set c.isFrontPage = false where c.courseId = :courseId and c.isFrontPage = true and c.contentId <> :keep")
    int clearFrontPageExcept(@Param("courseId") UUID courseId, @Param("keep") UUID keep);
}
