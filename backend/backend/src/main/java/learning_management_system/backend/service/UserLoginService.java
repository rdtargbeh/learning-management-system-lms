package learning_management_system.backend.service;

import learning_management_system.backend.dto.CreateUserLoginRequest;
import learning_management_system.backend.dto.UserLoginDto;

import java.util.List;
import java.util.UUID;

/**
 * Extra sign-in names of a user (Canvas "logins"), e.g. a student ID from the SIS. Each works like the username at
 * sign-in, with the account's password. Managed by USER_MANAGE.
 */
public interface UserLoginService {

    List<UserLoginDto> list(UUID userId);

    UserLoginDto add(UUID userId, CreateUserLoginRequest r);

    void remove(UUID userId, UUID loginId);
}
