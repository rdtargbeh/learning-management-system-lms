package learning_management_system.backend.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import learning_management_system.backend.enums.InteractionType;
import learning_management_system.backend.enums.LaunchMethod;

/**
 * How an interactive, SCORM or external-tool item launches (table interactive_content). As a PUT body it replaces the
 * details. LTI needs ltiToolId, SCORM needs scormPackageId, EMBED / IFRAME / NEW_WINDOW / XAPI need an https URL.
 */
public record InteractiveContentDto(InteractionType interactionType, @Size(max = 500) String interactionUrl, LaunchMethod launchMethod,
                                    @Size(max = 255) String ltiToolId, @Size(max = 255) String scormPackageId, Boolean allowsMultipleAttempts,
                                    @Positive Integer maxAttempts, @Positive Integer timeLimitMinutes, String interactionInstructions,
                                    String successCriteria) {}
