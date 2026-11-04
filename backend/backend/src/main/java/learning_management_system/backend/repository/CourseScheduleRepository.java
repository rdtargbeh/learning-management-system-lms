package learning_management_system.backend.repository;

import learning_management_system.backend.entity.CourseSchedule;
import learning_management_system.backend.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, UUID> {
    List<CourseSchedule> findBySectionId(UUID sectionId);

    List<CourseSchedule> findBySectionIdIn(Collection<UUID> sectionIds);

    Optional<CourseSchedule> findByScheduleIdAndSectionId(UUID scheduleId, UUID sectionId);

    /** Meetings on the same day whose time overlaps [start, end) (conflict checks). */
    @Query("""
        select m from CourseSchedule m
        where m.dayOfWeek = :day and m.timeStart < :end and m.timeEnd > :start
        """)
    List<CourseSchedule> overlapping(@Param("day") DayOfWeek day, @Param("start") LocalTime start, @Param("end") LocalTime end);
}
