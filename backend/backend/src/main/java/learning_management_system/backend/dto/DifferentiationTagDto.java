package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A staff-only label for a set of students in a course (table differentiation_tag), used to target work. */
public record DifferentiationTagDto(UUID tagId, UUID courseId, String tagName, String tagDescription, long memberCount, UUID createdBy,
                                    OffsetDateTime dateCreated) {}
