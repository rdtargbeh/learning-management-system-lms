package learning_management_system.backend.service;

import learning_management_system.backend.dto.AcademicAdvisorProfileDto;
import learning_management_system.backend.dto.CreateUserRequest;
import learning_management_system.backend.dto.EmployeeProfileDto;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.SetPasswordRequest;
import learning_management_system.backend.dto.StudentProfileDto;
import learning_management_system.backend.dto.UpdateUserRequest;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.dto.UserProfileDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.UserType;

import java.util.List;
import java.util.UUID;

/**
 * Institution (tenant) users and their profiles. All accounts are created through {@code users}; the database
 * trigger then creates user_profile plus the profile for the user_type (student, employee, advisor). Row-level
 * security limits every query to the caller's tenant. The {@code to...} methods build API views and do no
 * permission check of their own.
 */
public interface UserService {

    UserDetailDto create(CreateUserRequest r);

    PageResponse<UserSummaryDto> search(UserType type, AccountStatus status, String q, int page, int size);

    UserDetailDto get(UUID userId);

    UserDetailDto update(UUID userId, UpdateUserRequest r);

    UserDetailDto setStatus(UUID userId, AccountStatus status);

    void setPassword(UUID userId, SetPasswordRequest r);

    UserProfileDto updateProfile(UUID userId, UserProfileDto d);

    StudentProfileDto updateStudentProfile(UUID userId, StudentProfileDto d);

    EmployeeProfileDto updateEmployeeProfile(UUID userId, EmployeeProfileDto d);

    AcademicAdvisorProfileDto updateAdvisorProfile(UUID userId, AcademicAdvisorProfileDto d);

    /** API view of a user with the main user_profile fields. */
    UserSummaryDto toSummary(User u);

    /** Batch version for lists: one query for all profiles. */
    List<UserSummaryDto> toSummaries(List<User> list);

    /** A user with every profile that exists for them. */
    UserDetailDto toDetail(User u);
}
