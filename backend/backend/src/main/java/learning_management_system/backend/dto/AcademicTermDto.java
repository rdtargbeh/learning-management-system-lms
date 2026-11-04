package learning_management_system.backend.dto;

import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.TermType;

/** A teaching period with its key dates (table academic_term). */
public record AcademicTermDto(UUID termId, String termName, String termCode, TermType termType, LocalDate dateStart,
                              LocalDate dateEnd, LocalDate dateCensus, LocalDate dateAddDropDeadline,
                              LocalDate dateWithdrawDeadline, LocalDate dateGradesDue, String sisSourceId, Boolean isCurrent,
                              Boolean isActive) {}
