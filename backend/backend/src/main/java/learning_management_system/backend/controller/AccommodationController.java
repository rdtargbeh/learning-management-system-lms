package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AccommodationDto;
import learning_management_system.backend.dto.CourseAccommodationDto;
import learning_management_system.backend.dto.UpdateAccommodationRequest;
import learning_management_system.backend.service.AccommodationService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Accommodations: changing one, and the course staff's view of their students' accommodations. */
@RestController
@RequestMapping("/api/v1")
public class AccommodationController {
    private final AccommodationService accommodations;

    public AccommodationController(AccommodationService accommodations) { this.accommodations = accommodations; }

    @PatchMapping("/accommodations/{accommodationId}")
    public AccommodationDto update(@PathVariable UUID accommodationId, @Valid @RequestBody UpdateAccommodationRequest r) {
        return accommodations.update(accommodationId, r);
    }

    @GetMapping("/courses/{courseId}/accommodations")
    public List<CourseAccommodationDto> forCourse(@PathVariable UUID courseId) { return accommodations.forCourse(courseId); }
}
