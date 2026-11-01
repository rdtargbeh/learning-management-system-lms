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

    public UserServiceImplementation(UserRepository users, UserProfileRepository profiles,
                                     StudentProfileRepository students, EmployeeProfileRepository employees,
                                     AcademicAdvisorProfileRepository advisors, PasswordEncoder encoder,
                                     PlatformUserProfileRepository platformProfiles, UserProvisioningService provisioning,
                                     UserMapper userMapper) {
        this.users = users;
        this.profiles = profiles;
        this.students = students;
        this.employees = employees;
        this.advisors = advisors;
        this.encoder = encoder;
        this.platformProfiles = platformProfiles;
        this.provisioning = provisioning;
        this.userMapper = userMapper;
    }

    @Transactional
    @Override
    public UserDetailDto create(CreateUserRequest r) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        if (r.userType() == UserType.PLATFORM) {
            throw ApiException.badRequest("INVALID_USER_TYPE", "Platform users are created by platform administrators");
        }
        User u = provisioning.provisionTenantUser(r, Access.ctx().tenantId());   // the trigger creates the profiles
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
        u.setAccountStatus(status);
        u.setDateDeactivated(status == AccountStatus.DEACTIVATED ? OffsetDateTime.now() : null);
        if (status == AccountStatus.ACTIVE) { u.setAccountLockedUntil(null); u.setFailedLoginAttempts(0); }
        return toDetail(users.save(u));
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
    }

    // ---------------------------------------------------------------- profiles

    @Transactional
    @Override
    public UserProfileDto updateProfile(UUID userId, UserProfileDto d) {
        if (!Access.isSelf(userId)) Access.requireTenantPermission(Permissions.USER_MANAGE);
        load(userId);
        UserProfile p = profiles.findByUserId(userId).orElseThrow(() -> ApiException.notFound("Profile"));
        userMapper.updateEntity(d, p);
        return userMapper.toDto(profiles.save(p));
    }

    @Transactional
    @Override
    public StudentProfileDto updateStudentProfile(UUID userId, StudentProfileDto d) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        StudentProfile s = students.findByUserId(userId).orElseGet(() -> newProfile(userId, StudentProfile::new));
        userMapper.updateEntity(d, s);
        return userMapper.toDto(students.save(s));
    }

    @Transactional
    @Override
    public EmployeeProfileDto updateEmployeeProfile(UUID userId, EmployeeProfileDto d) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        EmployeeProfile e = employees.findByUserId(userId).orElseGet(() -> {
            if (d == null || d.employeeType() == null) {
                throw ApiException.badRequest("EMPLOYEE_TYPE_REQUIRED", "employeeType is required to add an employee profile");
            }
            return newProfile(userId, EmployeeProfile::new);
        });
        userMapper.updateEntity(d, e);
        return userMapper.toDto(employees.save(e));
    }

    @Transactional
    @Override
    public AcademicAdvisorProfileDto updateAdvisorProfile(UUID userId, AcademicAdvisorProfileDto d) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        AcademicAdvisorProfile a = advisors.findByUserId(userId).orElseGet(() -> {
            if (employees.findByUserId(userId).isEmpty()) {
                throw ApiException.badRequest("EMPLOYEE_PROFILE_REQUIRED", "An advisor must have an employee profile first");
            }
            return newProfile(userId, AcademicAdvisorProfile::new);
        });
        userMapper.updateEntity(d, a);
        return userMapper.toDto(advisors.save(a));
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

    private <P> P newProfile(UUID userId, java.util.function.Supplier<P> ctor) {
        User u = load(userId);
        P p = ctor.get();
        if (p instanceof StudentProfile s) { s.setUserId(userId); s.setTenantId(u.getTenantId()); }
        if (p instanceof EmployeeProfile e) { e.setUserId(userId); e.setTenantId(u.getTenantId()); }
        if (p instanceof AcademicAdvisorProfile a) { a.setUserId(userId); a.setTenantId(u.getTenantId()); }
        return p;
    }

    private User load(UUID userId) {
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        if (Boolean.TRUE.equals(u.getIsSystemUser())) throw ApiException.notFound("User");
        return u;
    }
}
