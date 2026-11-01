package learning_management_system.backend.service;

import learning_management_system.backend.dto.CreateObserverLinkRequest;
import learning_management_system.backend.dto.ObserverStudentLinkDto;

import java.util.List;
import java.util.UUID;

/** Who may follow which student (parents, mentors, athletics/success advisors). Observers have no separate profile. */
public interface ObserverService {

    ObserverStudentLinkDto link(CreateObserverLinkRequest r);

    List<ObserverStudentLinkDto> forStudent(UUID studentUserId);

    List<ObserverStudentLinkDto> forObserver(UUID observerUserId);

    ObserverStudentLinkDto revoke(UUID linkId);
}
