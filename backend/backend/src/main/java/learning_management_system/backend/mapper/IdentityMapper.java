package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.AccessibilitySettingsDto;
import learning_management_system.backend.dto.AccommodationDto;
import learning_management_system.backend.dto.AccountLockDto;
import learning_management_system.backend.dto.AuditLogDto;
import learning_management_system.backend.dto.CreateAccommodationRequest;
import learning_management_system.backend.dto.LoginHistoryDto;
import learning_management_system.backend.dto.SessionDto;
import learning_management_system.backend.dto.UpdateAccommodationRequest;
import learning_management_system.backend.dto.UserLoginDto;
import learning_management_system.backend.dto.UserPreferencesDto;
import learning_management_system.backend.entity.Accommodation;
import learning_management_system.backend.entity.AccountLock;
import learning_management_system.backend.entity.AuditLog;
import learning_management_system.backend.entity.LoginHistory;
import learning_management_system.backend.entity.UserAccessibilitySettings;
import learning_management_system.backend.entity.UserLogin;
import learning_management_system.backend.entity.UserPreferences;
import learning_management_system.backend.entity.UserSession;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.net.InetAddress;

/** Sessions, sign-in history, locks, audit log, accessibility, preferences, accommodations and extra logins. */
@Mapper(config = MapStructConfig.class)
public interface IdentityMapper {

    @Mapping(target = "current", source = "current")
    SessionDto toDto(UserSession session, boolean current);

    LoginHistoryDto toDto(LoginHistory login);

    AccountLockDto toDto(AccountLock lock);

    AuditLogDto toDto(AuditLog entry);

    AccessibilitySettingsDto toDto(UserAccessibilitySettings settings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AccessibilitySettingsDto dto, @MappingTarget UserAccessibilitySettings settings);

    UserPreferencesDto toDto(UserPreferences preferences);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UserPreferencesDto dto, @MappingTarget UserPreferences preferences);

    AccommodationDto toDto(Accommodation accommodation);

    Accommodation toEntity(CreateAccommodationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateAccommodationRequest request, @MappingTarget Accommodation accommodation);

    UserLoginDto toDto(UserLogin login);

    /** IP addresses travel as text. */
    default String ip(InetAddress address) {
        return address == null ? null : address.getHostAddress();
    }
}
