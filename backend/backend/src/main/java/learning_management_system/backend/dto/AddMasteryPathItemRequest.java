package learning_management_system.backend.dto;

import jakarta.validation.constraints.Positive;

import java.util.UUID;

/** Adds a content item or an assignment of the course to a range (setNumber groups items into alternative sets, default 1). */
public record AddMasteryPathItemRequest(UUID contentId, UUID assignmentId, @Positive Integer setNumber) {}
