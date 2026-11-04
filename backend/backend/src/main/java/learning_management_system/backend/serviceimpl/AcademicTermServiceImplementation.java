package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AcademicTermDto;
import learning_management_system.backend.dto.CreateAcademicTermRequest;
import learning_management_system.backend.dto.UpdateAcademicTermRequest;
import learning_management_system.backend.entity.AcademicTerm;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.AcademicTermMapper;
import learning_management_system.backend.repository.AcademicTermRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.AcademicTermService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AcademicTermServiceImplementation implements AcademicTermService {
    private final AcademicTermRepository terms;
    private final AcademicTermMapper termMapper;
    private final EntityRefresher refresher;

    public AcademicTermServiceImplementation(AcademicTermRepository terms, AcademicTermMapper termMapper,
                                             EntityRefresher refresher) {
        this.terms = terms;
        this.termMapper = termMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<AcademicTermDto> list() {
        Access.requireTenant();
        return terms.findAllByOrderByDateStartDesc().stream().map(termMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public AcademicTermDto current() {
        Access.requireTenant();
        return termMapper.toDto(terms.findFirstByIsCurrentTrue().orElseThrow(() -> ApiException.notFound("Current term")));
    }

    @Transactional
    @Override
    public AcademicTermDto create(CreateAcademicTermRequest r) {
        Access.requireTenantPermission(Permissions.ACADEMIC_STRUCTURE_MANAGE);
        AcademicTerm t = termMapper.toEntity(r);
        validateDates(t);
        refresher.refresh(terms.saveAndFlush(t));
        if (Boolean.TRUE.equals(t.getIsCurrent())) terms.clearCurrentExcept(t.getTermId());
        return termMapper.toDto(t);
    }

    @Transactional
    @Override
    public AcademicTermDto update(UUID termId, UpdateAcademicTermRequest r) {
        Access.requireTenantPermission(Permissions.ACADEMIC_STRUCTURE_MANAGE);
        AcademicTerm t = terms.findById(termId).orElseThrow(() -> ApiException.notFound("Term"));
        termMapper.updateEntity(r, t);
        validateDates(t);
        terms.saveAndFlush(t);
        if (Boolean.TRUE.equals(r.isCurrent())) terms.clearCurrentExcept(termId);
        return termMapper.toDto(terms.findById(termId).orElseThrow());
    }

    private static void validateDates(AcademicTerm t) {
        if (t.getDateEnd().isBefore(t.getDateStart())) {
            throw ApiException.badRequest("INVALID_DATES", "The term must end on or after its start date");
        }
    }
}
