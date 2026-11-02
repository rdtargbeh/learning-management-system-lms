package learning_management_system.backend.service;

import learning_management_system.backend.dto.PermissionDto;

import java.util.List;

/** permissions: the catalogue of permission codes roles can grant (maintained by the platform, read-only here). */
public interface PermissionService {

    List<PermissionDto> list();
}
