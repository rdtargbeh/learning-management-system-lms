package learning_management_system.backend.repository;

import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByTenantIdAndUsernameIgnoreCase(UUID tenantId, String username);
    Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);
    Optional<User> findByTenantIdIsNullAndUsernameIgnoreCase(String username);
    Optional<User> findByTenantIdIsNullAndEmail(String email);
    boolean existsByIsSystemUserTrue();
    List<User> findByIsSystemUserTrueOrderByUsername();

    /** Tenant user search; RLS already restricts rows to the caller's tenant. */
    @Query("""
        select u from User u
        where u.isSystemUser = false
          and (:type is null or u.userType = :type)
          and (:status is null or u.accountStatus = :status)
          and (:q is null or lower(u.username) like lower(concat('%', cast(:q as string), '%'))
                          or lower(cast(u.email as string)) like lower(concat('%', cast(:q as string), '%')))
        """)
    Page<User> search(@Param("type") UserType type, @Param("status") AccountStatus status,
                      @Param("q") String q, Pageable pageable);
}
