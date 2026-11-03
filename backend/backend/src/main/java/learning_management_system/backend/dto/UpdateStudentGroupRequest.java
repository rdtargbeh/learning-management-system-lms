package learning_management_system.backend.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** PATCH of a group: null leaves a value unchanged. isActive false closes the group to new members. */
public record UpdateStudentGroupRequest(@Size(min = 3, max = 255) String groupName, String groupDescription, @Positive Integer maxMembers,
                                        LocalDate dateStart, LocalDate dateEnd, Boolean isActive) {}
