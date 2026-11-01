package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import learning_management_system.backend.enums.ObserverStudentLinkAccessLevel;
import learning_management_system.backend.enums.Relationship;

/** Let an observer (parent, mentor ...) follow a student. */
public record CreateObserverLinkRequest(@NotNull UUID observerUserId, @NotNull UUID studentUserId,
                                        Relationship relationship, ObserverStudentLinkAccessLevel accessLevel,
                                        UUID consentRecordId) {}
