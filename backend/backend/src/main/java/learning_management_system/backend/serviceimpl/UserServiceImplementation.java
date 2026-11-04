package learning_management_system.backend.serviceimpl;

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
import learning_management_system.backend.entity.AcademicAdvisorProfile;
import learning_management_system.backend.entity.EmployeeProfile;
import learning_management_system.backend.entity.StudentProfile;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.entity.UserProfile;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.UserMapper;
import learning_management_system.backend.repository.AcademicAdvisorProfileRepository;
import learning_management_system.backend.repository.EmployeeProfileRepository;
import learning_management_system.backend.repository.PlatformUserProfileRepository;
import learning_management_system.backend.repository.StudentProfileRepository;
import learning_management_system.backend.repository.UserProfileRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.UserSessionService;
import learning_management_system.backend.service.TenantUserLimitService;
import learning_management_system.backend.service.UserProvisioningService;
import learning_management_system.backend.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static learning_management_system.backend.utility.Patch.set;

/**
 * Institution (tenant) users. All accounts are created through {@code users}; the database trigger then creates
 * user_profile plus the profile for the user_type (student, employee, advisor). Row-level security limits every
 * query to the caller's tenant.
 */
@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final StudentProfileRepository students;
    private final EmployeeProfileRepository employees;
    private final AcademicAdvisorProfileRepository advisors;
    private final PasswordEncoder encoder;
    private final PlatformUserProfileRepository platformProfiles;
    private final UserProvisioningService provisioning;
    private final UserMapper userMapper;
    private final TenantUserLimitService userLimits;
    private final UserSessionService sessions;
    private final AuditLogService audit;

    public UserServiceImplementation(UserRepository users, UserProfileRepository profiles,
                                     StudentProfileRepository students, EmployeeProfileRepository employees,
                                     AcademicAdvisorProfileRepository advisors, PasswordEncoder encoder,
                                     PlatformUserProfileRepository platformProfiles, UserProvisioningService provisioning,
                                     UserMapper userMapper, TenantUserLimitService userLimits, UserSessionService sessions, AuditLogService audit) {
        this.users = users;
        this.profiles = profiles;
        this.students = students;
        this.employees = employees;
        this.advisors = advisors;
        this.encoder = encoder;
        this.platformProfiles = platformProfiles;
        this.provisioning = provisioning;
        this.userMapper = userMapper;
        this.userLimits = userLimits;
        this.sessions = sessions;
        this.audit = audit;
    }

    @Transactional
    @Override
    public UserDetailDto create(CreateUserRequest r) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        if (r.userType() == UserType.PLATFORM) {
            throw ApiException.badRequest("INVALID_USER_TYPE", "Platform users are created by platform administrators");
        }
        User u = provisioning.provisionTenantUser(r, Access.ctx().tenantId());   // the trigger creates the profiles
        audit.record("USER_CREATED", "USER", u.getUserId(), u.getUserId(), Severity.INFO, null,
                Map.of("username", u.getUsername(), "userType", u.getUserType().name()));
        return toDetail(u);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<UserSummaryDto> search(UserType type, AccountStatus status, String q, int page, int size) {
        Access.requireTenantPermission(Permissions.USER_READ);
        Page<User> result = users.search(type, status, q == null || q.isBlank() ? null : q.trim(),
                PageRequest.of(page, Math.min(size, 200), Sort.by("username")));
        return PageResponse.of(new PageImpl<>(toSummaries(result.getContent()), result.getPageable(), result.getTotalElements()),
                s -> s);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetailDto get(UUID userId) {
        if (!Access.isSelf(userId)) Access.requireTenantPermission(Permissions.USER_READ);
        return toDetail(load(userId));
    }

    @Transactional
    @Override
    public UserDetailDto update(UUID userId, UpdateUserRequest r) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        User u = load(userId);
        set(r.username(), v -> u.setUsername(v.trim()));
        set(r.email(), v -> u.setEmail(v.trim()));
        set(r.sisSourceId(), u::setSisSourceId);
        set(r.mfaRequired(), u::setMfaRequired);
        return toDetail(users.save(u));
    }

    @Transactional
    @Override
    public UserDetailDto setStatus(UUID userId, AccountStatus status) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        if (Access.isSelf(userId)) throw ApiException.badRequest("SELF_STATUS", "You cannot change your own account status");
        User u = load(userId);
        boolean wasCounted = u.getAccountStatus() != AccountStatus.DEACTIVATED && u.getAccountStatus() != AccountStatus.DELETED;
        boolean counted = status != AccountStatus.DEACTIVATED && status != AccountStatus.DELETED;
        if (counted && !wasCounted) userLimits.requireRoom(u.getTenantId(), u.getUserType());     // reactivation uses a seat
        u.setAccountStatus(status);
        u.setDateDeactivated(status == AccountStatus.DEACTIVATED ? OffsetDateTime.now() : null);
        if (status == AccountStatus.ACTIVE) { u.setAccountLockedUntil(null); u.setFailedLoginAttempts(0); }
        users.saveAndFlush(u);
        userLimits.refreshCounts(u.getTenantId());
        if (status != AccountStatus.ACTIVE) sessions.revokeAll(userId);          // signed out everywhere
        audit.record("USER_STATUS_CHANGED", "USER", userId, userId, status == AccountStatus.ACTIVE ? Severity.INFO : Severity.WARNING,
                null, Map.of("status", status.name()));
        return toDetail(u);
    }

    @Transactional
    @Override
    public void setPassword(UUID userId, SetPasswordRequest r) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        User u = load(userId);
        u.setPasswordHash(encoder.encode(r.newPassword()));
        u.setPasswordLastChanged(OffsetDateTime.now());
        u.setMustChangePassword(r.mustChangePassword() == null || r.mustChangePassword());
        if (u.getAccountStatus() == AccountStatus.INVITED) u.setAccountStatus(AccountStatus.ACTIVE);
        users.save(u);
        sessions.revokeAll(userId);
        audit.record("PASSWORD_SET_BY_ADMIN", "USER", userId, userId, Severity.WARNING, null, null);
    }

    // ---------------------------------------------------------------- views (no permission check: callers decide)

    /** API view of a user with the main user_profile fields. */
    @Override
    public UserSummaryDto toSummary(User u) {
        return userMapper.toSummaryDto(u, profiles.findByUserId(u.getUserId()).orElse(null));
    }

    /** Batch version for lists: one query for all profiles. */
    @Override
    public List<UserSummaryDto> toSummaries(List<User> list) {
        Map<UUID, UserProfile> byUser = profiles.findByUserIdIn(list.stream().map(User::getUserId).toList()).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
        return list.stream().map(u -> userMapper.toSummaryDto(u, byUser.get(u.getUserId()))).toList();
    }

    /** A user with every profile that exists for them. */
    @Override
    public UserDetailDto toDetail(User u) {
        UUID id = u.getUserId();
        return new UserDetailDto(toSummary(u),
                userMapper.toDto(profiles.findByUserId(id).orElse(null)),
                userMapper.toDto(students.findByUserId(id).orElse(null)),
                userMapper.toDto(employees.findByUserId(id).orElse(null)),
                userMapper.toDto(advisors.findByUserId(id).orElse(null)),
                userMapper.toDto(platformProfiles.findByUserId(id).orElse(null)));
    }

    @Override
    public Map<UUID, UserSummaryDto> summariesById(Collection<UUID> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return toSummaries(users.findAllById(new HashSet<>(userIds))).stream()
                .collect(Collectors.toMap(UserSummaryDto::userId, Function.identity()));
    }

    private User load(UUID userId) {
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        if (Boolean.TRUE.equals(u.getIsSystemUser())) throw ApiException.notFound("User");
        return u;
    }
}
