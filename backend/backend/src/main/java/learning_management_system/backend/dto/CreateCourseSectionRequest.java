package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.DeliveryMode;

/** Create a section. termId defaults to the course's term. capacity null = unlimited. */
public record CreateCourseSectionRequest(@NotBlank @Size(max = 10) String sectionNumber, UUID termId, String sisSourceId,
                                         @Positive Integer capacity, DeliveryMode deliveryMode, LocalDate dateStart,
                                         LocalDate dateEnd, LocalDate registrationOpenDate, LocalDate registrationCloseDate,
                                         Boolean isOpenForEnrollment) {}
