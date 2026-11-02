package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AccountLockDto;
import learning_management_system.backend.dto.LockAccountRequest;
import learning_management_system.backend.service.AccountLockService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** account_lock: administrator and automatic locks of an account. */
@RestController
@RequestMapping("/api/v1/users/{userId}")
public class AccountLockController {
    private final AccountLockService locks;

    public AccountLockController(AccountLockService locks) { this.locks = locks; }

    @GetMapping("/lock")
    public AccountLockDto get(@PathVariable UUID userId) { return locks.get(userId); }

    @PostMapping("/lock")
    public AccountLockDto lock(@PathVariable UUID userId, @Valid @RequestBody LockAccountRequest r) { return locks.lock(userId, r); }

    @PostMapping("/unlock")
    public AccountLockDto unlock(@PathVariable UUID userId) { return locks.unlock(userId); }
}
