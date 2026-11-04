package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import learning_management_system.backend.enums.TermType;

/** Create a term. isCurrent = true makes it the institution's current term. */
public record CreateAcademicTermRequest(@NotBlank @Size(max = 100) String termName, @NotBlank @Size(max = 50) String termCode,
                                        TermType termType, @NotNull LocalDate dateStart, @NotNull LocalDate dateEnd,
                                        LocalDate dateCensus, LocalDate dateAddDropDeadline, LocalDate dateWithdrawDeadline,
                                        LocalDate dateGradesDue, String sisSourceId, Boolean isCurrent) {}
