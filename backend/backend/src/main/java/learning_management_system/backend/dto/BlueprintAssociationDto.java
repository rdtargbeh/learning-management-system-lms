package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A course that receives a blueprint course's content (table blueprint_association). */
public record BlueprintAssociationDto(UUID blueprintAssociationId, UUID blueprintCourseId, UUID associatedCourseId, String courseCode,
                                      String courseTitle, OffsetDateTime dateLastSynced, Boolean isActive, OffsetDateTime dateCreated) {}
