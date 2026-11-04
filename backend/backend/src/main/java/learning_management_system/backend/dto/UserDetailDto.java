package learning_management_system.backend.dto;

/** A user with every profile that exists for them (only the ones that apply are filled). */
public record UserDetailDto(UserSummaryDto user, UserProfileDto profile, StudentProfileDto studentProfile,
                            EmployeeProfileDto employeeProfile, AcademicAdvisorProfileDto advisorProfile,
                            PlatformUserProfileDto platformProfile) {}
