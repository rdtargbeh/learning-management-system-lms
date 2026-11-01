package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CreateObserverLinkRequest;
import learning_management_system.backend.dto.ObserverStudentLinkDto;
import learning_management_system.backend.entity.ObserverStudentLink;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.LinkStatus;
import learning_management_system.backend.enums.ObserverStudentLinkAccessLevel;
import learning_management_system.backend.enums.Relationship;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.ObserverStudentLinkMapper;
import learning_management_system.backend.repository.ObserverStudentLinkRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.ObserverService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Who may follow which student (parents, mentors, athletics/success advisors). Observers have no separate profile. */
@Service
public class ObserverServiceImplementation implements ObserverService {
    private final ObserverStudentLinkRepository links;
    private final UserRepository users;
    private final ObserverStudentLinkMapper linkMapper;

    public ObserverServiceImplementation(ObserverStudentLinkRepository links, UserRepository users,
                                         ObserverStudentLinkMapper linkMapper) {
        this.links = links;
        this.users = users;
        this.linkMapper = linkMapper;
    }

    @Transactional
    @Override
    public ObserverStudentLinkDto link(CreateObserverLinkRequest r) {
        Access.requireTenantPermission(Permissions.OBSERVER_MANAGE);
        User student = users.findById(r.studentUserId()).orElseThrow(() -> ApiException.notFound("Student"));
        if (student.getUserType() != UserType.STUDENT) throw ApiException.badRequest("NOT_A_STUDENT", "Only students can be observed");
        users.findById(r.observerUserId()).filter(u -> !Boolean.TRUE.equals(u.getIsSystemUser()))
                .orElseThrow(() -> ApiException.notFound("Observer"));
        ObserverStudentLink l = new ObserverStudentLink();
        l.setObserverUserId(r.observerUserId());
        l.setStudentUserId(r.studentUserId());
        l.setRelationship(r.relationship() == null ? Relationship.PARENT : r.relationship());
        l.setAccessLevel(r.accessLevel() == null ? ObserverStudentLinkAccessLevel.VIEW_COURSES : r.accessLevel());
        l.setConsentRecordId(r.consentRecordId());
        l.setCreatedBy(Access.ctx().userId());
        return linkMapper.toDto(links.saveAndFlush(l));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ObserverStudentLinkDto> forStudent(UUID studentUserId) {
        if (!Access.isSelf(studentUserId)) Access.requireTenantPermission(Permissions.USER_READ);
        return links.findByStudentUserId(studentUserId).stream().map(linkMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ObserverStudentLinkDto> forObserver(UUID observerUserId) {
        if (!Access.isSelf(observerUserId)) Access.requireTenantPermission(Permissions.USER_READ);
        return links.findByObserverUserId(observerUserId).stream().map(linkMapper::toDto).toList();
    }

    @Transactional
    @Override
    public ObserverStudentLinkDto revoke(UUID linkId) {
        Access.requireTenantPermission(Permissions.OBSERVER_MANAGE);
        ObserverStudentLink l = links.findById(linkId).orElseThrow(() -> ApiException.notFound("Observer link"));
        l.setLinkStatus(LinkStatus.REVOKED);
        l.setDateRevoked(OffsetDateTime.now());
        return linkMapper.toDto(links.save(l));
    }
}
