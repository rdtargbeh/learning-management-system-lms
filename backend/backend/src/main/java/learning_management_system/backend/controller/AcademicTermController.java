package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AcademicTermDto;
import learning_management_system.backend.dto.CreateAcademicTermRequest;
import learning_management_system.backend.dto.UpdateAcademicTermRequest;
import learning_management_system.backend.service.AcademicTermService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Academic terms. */
@RestController
@RequestMapping("/api/v1/terms")
public class AcademicTermController {
    private final AcademicTermService terms;

    public AcademicTermController(AcademicTermService terms) { this.terms = terms; }

    @GetMapping
    public List<AcademicTermDto> list() { return terms.list(); }

    @GetMapping("/current")
    public AcademicTermDto current() { return terms.current(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AcademicTermDto create(@Valid @RequestBody CreateAcademicTermRequest r) { return terms.create(r); }

    @PatchMapping("/{termId}")
    public AcademicTermDto update(@PathVariable UUID termId, @Valid @RequestBody UpdateAcademicTermRequest r) {
        return terms.update(termId, r);
    }
}
