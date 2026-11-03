package learning_management_system.backend.repository;

import learning_management_system.backend.entity.ContentBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentBlockRepository extends JpaRepository<ContentBlock, UUID> {
    List<ContentBlock> findByContentIdOrderByDisplayOrderAscBlockNumberAsc(UUID contentId);

    @Query("select coalesce(max(b.blockNumber), 0) from ContentBlock b where b.contentId = :contentId")
    int maxBlockNumber(@Param("contentId") UUID contentId);
}
