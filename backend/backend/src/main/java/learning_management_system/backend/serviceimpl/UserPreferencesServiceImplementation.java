package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.UserPreferencesDto;
import learning_management_system.backend.entity.UserPreferences;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.IdentityMapper;
import learning_management_system.backend.repository.UserPreferencesRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.UserPreferencesService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserPreferencesServiceImplementation implements UserPreferencesService {
    private final UserPreferencesRepository preferences;
    private final UserRepository users;
    private final IdentityMapper identityMapper;
    private final EntityRefresher refresher;

    public UserPreferencesServiceImplementation(UserPreferencesRepository preferences, UserRepository users,
                                                IdentityMapper identityMapper, EntityRefresher refresher) {
        this.preferences = preferences;
        this.users = users;
        this.identityMapper = identityMapper;
        this.refresher = refresher;
    }

    @Transactional
    @Override
    public UserPreferencesDto get(UUID userId) {
        UUID id = userId == null ? me() : userId;
        if (!Access.isSelf(id)) Access.requireTenantPermission(Permissions.USER_READ);
        users.findById(id).orElseThrow(() -> ApiException.notFound("User"));
        return identityMapper.toDto(row(id));
    }

    @Transactional
    @Override
    public UserPreferencesDto update(UserPreferencesDto d) {
        UserPreferences p = row(me());
        identityMapper.updateEntity(d, p);
        preferences.saveAndFlush(p);
        return identityMapper.toDto(refresher.refresh(p));
    }

    /** Rows are created on first use with the database defaults. */
    private UserPreferences row(UUID userId) {
        return preferences.findByUserId(userId).orElseGet(() -> {
            UserPreferences p = new UserPreferences();
            p.setUserId(userId);
            return refresher.refresh(preferences.saveAndFlush(p));
        });
    }

    private static UUID me() {
        UUID id = Access.ctx().userId();
        if (id == null) throw ApiException.unauthorized("Sign in first");
        return id;
    }
}
