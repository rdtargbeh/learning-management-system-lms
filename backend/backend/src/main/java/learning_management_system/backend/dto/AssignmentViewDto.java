package learning_management_system.backend.dto;

/** An assignment, plus the caller's own status when the caller is a student (null for staff). */
public record AssignmentViewDto(AssignmentDto assignment, StudentAssignmentStatusDto myStatus) {}
