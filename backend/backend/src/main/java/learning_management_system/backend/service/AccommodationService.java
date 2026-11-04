package learning_management_system.backend.service;

import learning_management_system.backend.dto.AccommodationDto;
import learning_management_system.backend.dto.CourseAccommodationDto;
import learning_management_system.backend.dto.CreateAccommodationRequest;
import learning_management_system.backend.dto.UpdateAccommodationRequest;

import java.util.List;
import java.util.UUID;

/**
 * Student accommodations (extended time, alternate formats ...). Disability services staff (ACCOMMODATION_MANAGE)
 * approve and manage them; students see their own; course staff see the active ones of their students, without the
 * details of confidential ones.
 */
public interface AccommodationService {

    AccommodationDto create(UUID userId, CreateAccommodationRequest r);

    List<AccommodationDto> forUser(UUID userId);

    AccommodationDto update(UUID accommodationId, UpdateAccommodationRequest r);

    /** Active accommodations of the course's students that apply to this course (or to every course) today. */
    List<CourseAccommodationDto> forCourse(UUID courseId);
}
