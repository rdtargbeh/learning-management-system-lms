package learning_management_system.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.AcademicLevel;
import learning_management_system.backend.enums.AcademicStanding;
import learning_management_system.backend.enums.StudentProfileEnrollmentStatus;

/** Academic details of a student (table student_profile). */
public record StudentProfileDto(String studentNumber, UUID orgUnitId, AcademicLevel academicLevel, String programName,
                                String major, String minor, String concentration, StudentProfileEnrollmentStatus enrollmentStatus,
                                AcademicStanding academicStanding, Boolean isFullTime, BigDecimal cumulativeGpa,
                                BigDecimal totalCreditsCompleted, BigDecimal totalCreditsRequired, LocalDate dateAdmitted,
                                UUID admitTermId, UUID expectedGraduationTermId, LocalDate dateGraduated, UUID advisorUserId,
                                Boolean ferpaDirectoryHold) {}
