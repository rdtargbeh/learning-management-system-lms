package learning_management_system.backend.dto;

import java.util.List;

/** Outcome of an automatic group assignment: studentsUnassigned are those left over because every group was full. */
public record AutoAssignGroupsResultDto(int groupsCreated, int studentsAssigned, int studentsUnassigned, List<StudentGroupDto> groups) {}
