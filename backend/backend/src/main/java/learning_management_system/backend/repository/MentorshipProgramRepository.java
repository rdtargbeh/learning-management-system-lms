package learning_management_system.backend.repository;

import learning_management_system.backend.entity.MentorshipProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/** Generated repository; add custom queries in a hand-written repository if needed. */
@Repository
public interface MentorshipProgramRepository extends JpaRepository<MentorshipProgram, UUID> {
}
