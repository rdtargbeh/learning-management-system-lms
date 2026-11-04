package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.AcademicAdvisorProfileDto;
import learning_management_system.backend.dto.EmployeeProfileDto;
import learning_management_system.backend.dto.PlatformUserProfileDto;
import learning_management_system.backend.dto.StudentProfileDto;
import learning_management_system.backend.dto.UserProfileDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.AcademicAdvisorProfile;
import learning_management_system.backend.entity.EmployeeProfile;
import learning_management_system.backend.entity.PlatformUserProfile;
import learning_management_system.backend.entity.StudentProfile;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.entity.UserProfile;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * users + the profile tables. The {@code update...} methods apply PATCH semantics: a null value in the DTO leaves the
 * column unchanged.
 */
@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    // ---------------------------------------------------------------- entity -> DTO

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "tenantId", source = "user.tenantId")
    @Mapping(target = "systemUser", expression = "java(Boolean.TRUE.equals(user.getIsSystemUser()))")
    @Mapping(target = "userType", source = "user.userType")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "accountStatus", source = "user.accountStatus")
    @Mapping(target = "displayName", source = "profile.displayName")
    @Mapping(target = "firstName", source = "profile.firstName")
    @Mapping(target = "lastName", source = "profile.lastName")
    @Mapping(target = "lastLogin", source = "user.lastLogin")
    @Mapping(target = "dateCreated", source = "user.dateCreated")
    UserSummaryDto toSummaryDto(User user, UserProfile profile);

    UserProfileDto toDto(UserProfile profile);

    StudentProfileDto toDto(StudentProfile profile);

    EmployeeProfileDto toDto(EmployeeProfile profile);

    AcademicAdvisorProfileDto toDto(AcademicAdvisorProfile profile);

    PlatformUserProfileDto toDto(PlatformUserProfile profile);

    // ---------------------------------------------------------------- PATCH DTO -> entity

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UserProfileDto dto, @MappingTarget UserProfile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(StudentProfileDto dto, @MappingTarget StudentProfile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(EmployeeProfileDto dto, @MappingTarget EmployeeProfile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AcademicAdvisorProfileDto dto, @MappingTarget AcademicAdvisorProfile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "dateLastAccess", ignore = true)
    void updateEntity(PlatformUserProfileDto dto, @MappingTarget PlatformUserProfile profile);

    /** When no display name was ever given, build one from the preferred/first name and last name. */
    @AfterMapping
    default void defaultDisplayName(UserProfileDto dto, @MappingTarget UserProfile p) {
        if (dto == null || dto.displayName() != null || p.getDisplayName() != null) return;
        if (p.getFirstName() == null && p.getLastName() == null) return;
        String first = p.getPreferredName() != null ? p.getPreferredName() : nz(p.getFirstName());
        p.setDisplayName((first + " " + nz(p.getLastName())).trim());
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
