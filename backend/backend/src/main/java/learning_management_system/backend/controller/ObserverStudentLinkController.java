package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CreateObserverLinkRequest;
import learning_management_system.backend.dto.ObserverStudentLinkDto;
import learning_management_system.backend.service.ObserverStudentLinkService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** observer_student_link: who (parents, mentors ...) may follow which student. */
@RestController
@RequestMapping("/api/v1")
public class ObserverStudentLinkController {
    private final ObserverStudentLinkService observers;

    public ObserverStudentLinkController(ObserverStudentLinkService observers) { this.observers = observers; }

    @PostMapping("/observer-links")
    @ResponseStatus(HttpStatus.CREATED)
    public ObserverStudentLinkDto link(@Valid @RequestBody CreateObserverLinkRequest r) { return observers.link(r); }

    @PostMapping("/observer-links/{linkId}/revoke")
    public ObserverStudentLinkDto revoke(@PathVariable UUID linkId) { return observers.revoke(linkId); }

    @GetMapping("/users/{userId}/observers")
    public List<ObserverStudentLinkDto> observersOf(@PathVariable UUID userId) { return observers.forStudent(userId); }

    @GetMapping("/users/{userId}/observing")
    public List<ObserverStudentLinkDto> observing(@PathVariable UUID userId) { return observers.forObserver(userId); }
}
