package learning_management_system.backend.dto;

import jakarta.validation.constraints.Size;

/** A student's submission. draft = true saves it without handing it in. */
public record SubmitAssignmentRequest(String submissionText, @Size(max = 500) String submissionUrl, String submissionNotes,
                                      Boolean draft) {}
