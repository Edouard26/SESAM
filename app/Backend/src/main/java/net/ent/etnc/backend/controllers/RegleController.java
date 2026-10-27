package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.dtos.assemblers.RegleAssembler;
import net.ent.etnc.backend.dtos.requests.RegleRequest;
import net.ent.etnc.backend.dtos.responses.RegleResponse;
import net.ent.etnc.backend.services.RegleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rules/")
public class RegleController {

    private final RegleService regleService;
    private final RegleAssembler assembler;

    @Autowired
    public RegleController(RegleService regleService, RegleAssembler regleAssembler) {
        this.regleService = regleService;
        this.assembler = regleAssembler;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<RegleResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(regleService.findAll(pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<RegleResponse> getById(@PathVariable Long id) {
        return regleService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toResponse(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<RegleResponse> post(@RequestBody RegleRequest regleRequest) {
        return ResponseEntity.ok(assembler.toResponse(regleService.create(assembler.toEntity(regleRequest))));
    }
}
