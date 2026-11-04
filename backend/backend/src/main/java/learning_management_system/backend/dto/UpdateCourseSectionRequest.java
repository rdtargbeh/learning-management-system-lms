package learning_management_system.backend.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.CourseSectionStatus;
import learning_management_system.backend.enums.DeliveryMode;

/** PATCH a section; null fields are left unchanged. Capacity cannot go below the current enrollment. */
public record UpdateCourseSectionRequest(@Size(max = 10) String sectionNumber, UUID termId, String sisSourceId,
                                         @Positive Integer capacity, DeliveryMode deliveryMode, LocalDate dateStart,
                                         LocalDate dateEnd, LocalDate registrationOpenDate, LocalDate registrationCloseDate,
                                         Boolean isOpenForEnrollment, CourseSectionStatus status) {}
