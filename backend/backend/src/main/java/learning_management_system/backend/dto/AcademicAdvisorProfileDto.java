package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import learning_management_system.backend.enums.AdvisorType;

/** Advising details of an employee who advises students (table academic_advisor_profile). */
public record AcademicAdvisorProfileDto(AdvisorType advisorType, String specialization, String focusArea,
                                        Integer maxAdvisees, Boolean acceptingNewAdvisees, String availabilityNote,
                                        JsonNode certifications, Integer yearsExperience) {}
