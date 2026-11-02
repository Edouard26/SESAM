package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.dtos.assemblers.RegleAssembler;
import net.ent.etnc.backend.dtos.requests.RegleRequest;
import net.ent.etnc.backend.dtos.responses.RegleResponse;
import net.ent.etnc.backend.commons.RsqlFilterUtils;
import net.ent.etnc.backend.models.entities.Regle;
import net.ent.etnc.backend.services.RegleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rules/")
public class RegleController {

    private static final Map<String, String> FILTERABLE = Map.of(
            "nom", "nom",
            "processusNom", "processuss.nom"
    );

    private final RegleService regleService;
    private final RegleAssembler assembler;
    private final RsqlFilterUtils rsqlFilterUtils;

    @Autowired
    public RegleController(RegleService regleService, RegleAssembler regleAssembler, RsqlFilterUtils rsqlFilterUtils) {
        this.regleService = regleService;
        this.assembler = regleAssembler;
        this.rsqlFilterUtils = rsqlFilterUtils;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTIONNAIRE', 'FORMATEUR', 'ADMINISTRATEUR')")
    public ResponseEntity<Page<RegleResponse>> getAll(
            @RequestParam(required = false) String filter,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(regleService.findAll(rsqlFilterUtils.build(filter, FILTERABLE), pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasAnyRole('GESTIONNAIRE', 'FORMATEUR', 'ADMINISTRATEUR')")
    public ResponseEntity<RegleResponse> getById(@PathVariable Long id) {
        return regleService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toResponse(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GESTIONNAIRE', 'FORMATEUR', 'ADMINISTRATEUR')")
    public ResponseEntity<RegleResponse> post(@RequestBody RegleRequest regleRequest) {
        return ResponseEntity.ok(assembler.toResponse(regleService.create(assembler.toEntity(regleRequest))));
    }

    @PatchMapping("{id}/")
    @PreAuthorize("hasAnyRole('GESTIONNAIRE', 'FORMATEUR', 'ADMINISTRATEUR')")
    public ResponseEntity<RegleResponse> put(@PathVariable Long id, @RequestBody RegleRequest request) {
        Optional<Regle> existing = regleService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        var regle = assembler.toEntity(request);
        regle.setId(id);
        return ResponseEntity.ok(assembler.toResponse(regleService.update(regle)));
    }

    @DeleteMapping("{id}/")
    @PreAuthorize("hasAnyRole('GESTIONNAIRE', 'FORMATEUR', 'ADMINISTRATEUR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!regleService.existsById(id)) return ResponseEntity.notFound().build();
        regleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
