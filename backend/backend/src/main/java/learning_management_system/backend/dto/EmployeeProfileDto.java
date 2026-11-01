package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.AcademicRank;
import learning_management_system.backend.enums.EmployeeType;
import learning_management_system.backend.enums.EmploymentType;
import learning_management_system.backend.enums.StaffCategory;

/** Instructors, TAs and staff (table employee_profile). */
public record EmployeeProfileDto(String employeeNumber, EmployeeType employeeType, UUID orgUnitId, String jobTitle,
                                 EmploymentType employmentType, StaffCategory staffCategory,
                                 AcademicRank academicRank, String officeLocation, String officePhone,
                                 String officeHoursNote, JsonNode expertiseAreas, JsonNode degrees, String researchInterests,
                                 Integer maxCoursesPerTerm, LocalDate dateHired, LocalDate dateSeparated) {}
