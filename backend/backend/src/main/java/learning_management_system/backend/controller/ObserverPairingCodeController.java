package learning_management_system.backend.controller;

import learning_management_system.backend.dto.ObserverPairingCodeDto;
import learning_management_system.backend.dto.ObserverStudentLinkDto;
import learning_management_system.backend.dto.RedeemPairingCodeRequest;
import learning_management_system.backend.service.ObserverPairingCodeService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** observer_pairing_code: students create codes; observers redeem them. */
@RestController
@RequestMapping("/api/v1")
public class ObserverPairingCodeController {
    private final ObserverPairingCodeService codes;

    public ObserverPairingCodeController(ObserverPairingCodeService codes) { this.codes = codes; }

    @PostMapping("/users/{userId}/pairing-codes")
    @ResponseStatus(HttpStatus.CREATED)
    public ObserverPairingCodeDto create(@PathVariable UUID userId) { return codes.create(userId); }

    @GetMapping("/users/{userId}/pairing-codes")
    public List<ObserverPairingCodeDto> list(@PathVariable UUID userId) { return codes.list(userId); }

    @PostMapping("/observer-links/pair")
    @ResponseStatus(HttpStatus.CREATED)
    public ObserverStudentLinkDto redeem(@Valid @RequestBody RedeemPairingCodeRequest r) { return codes.redeem(r); }
}
