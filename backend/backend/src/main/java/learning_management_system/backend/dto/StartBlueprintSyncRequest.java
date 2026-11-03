package learning_management_system.backend.dto;

import jakarta.validation.constraints.Size;

/** Pushes the blueprint to every associated course. includeCourseSettings also copies syllabus, home page and grading settings. */
public record StartBlueprintSyncRequest(@Size(max = 2000) String syncComment, Boolean includeCourseSettings, Boolean notifyUsers) {}
