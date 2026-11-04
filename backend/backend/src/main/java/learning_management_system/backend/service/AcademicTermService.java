package learning_management_system.backend.service;

import learning_management_system.backend.dto.AcademicTermDto;
import learning_management_system.backend.dto.CreateAcademicTermRequest;
import learning_management_system.backend.dto.UpdateAcademicTermRequest;

import java.util.List;
import java.util.UUID;

/** Academic terms and their key dates. One term per institution can be marked current. */
public interface AcademicTermService {

    List<AcademicTermDto> list();

    /** The current term, or 404 when none is marked current. */
    AcademicTermDto current();

    AcademicTermDto create(CreateAcademicTermRequest r);

    AcademicTermDto update(UUID termId, UpdateAcademicTermRequest r);
}
