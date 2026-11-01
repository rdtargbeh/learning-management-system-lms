package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

/** Create a group in a set. sectionId is required; maxMembers defaults to the set's limit. */
public record CreateStudentGroupRequest(@NotBlank @Size(min = 3, max = 255) String groupName, String groupDescription,
                                        @NotNull UUID sectionId, @Positive Integer maxMembers, LocalDate dateStart,
                                        LocalDate dateEnd) {}
