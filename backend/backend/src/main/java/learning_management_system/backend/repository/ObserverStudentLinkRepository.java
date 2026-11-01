package learning_management_system.backend.repository;

import learning_management_system.backend.entity.ObserverStudentLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ObserverStudentLinkRepository extends JpaRepository<ObserverStudentLink, UUID> {
    List<ObserverStudentLink> findByStudentUserId(UUID studentUserId);
    List<ObserverStudentLink> findByObserverUserId(UUID observerUserId);
}
