package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.commons.RsqlFilterUtils;
import net.ent.etnc.backend.dtos.requests.StageRequest;
import net.ent.etnc.backend.dtos.responses.StageResponse;
import net.ent.etnc.backend.dtos.assemblers.StageAssembler;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.services.StageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * TODO
 */
@RestController
@RequestMapping("/api/v1/stages/")
public class StageController {

    private static final Map<String, String> FILTERABLE = Map.of(
            "nom", "nom",
            "statut", "statut"
    );

    private final StageService stageService;
    private final StageAssembler assembler;

    private final RsqlFilterUtils rsqlFilterUtils;

    @Autowired
    public StageController(StageService stageService, StageAssembler assembler, RsqlFilterUtils rsqlFilterUtils) {
        this.stageService = stageService;
        this.assembler = assembler;
        this.rsqlFilterUtils = rsqlFilterUtils;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<StageResponse>> getAll(
            @RequestParam(required = false) String filter,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Specification<Stage> spec = this.rsqlFilterUtils.build(filter, FILTERABLE);
        return ResponseEntity.ok(stageService.findAll(spec, pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<StageResponse> getById(@PathVariable Long id) {
        return stageService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toResponse(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<StageResponse> post(@RequestBody StageRequest stageRequest) {
        return ResponseEntity.ok(assembler.toResponse(stageService.create(assembler.toEntity(stageRequest))));
    }
}