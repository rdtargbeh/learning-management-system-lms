package learning_management_system.backend.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import learning_management_system.backend.enums.TermType;

/** PATCH a term; null fields are left unchanged. */
public record UpdateAcademicTermRequest(@Size(max = 100) String termName, @Size(max = 50) String termCode, TermType termType,
                                        LocalDate dateStart, LocalDate dateEnd, LocalDate dateCensus,
                                        LocalDate dateAddDropDeadline, LocalDate dateWithdrawDeadline, LocalDate dateGradesDue,
                                        String sisSourceId, Boolean isCurrent, Boolean isActive) {}
