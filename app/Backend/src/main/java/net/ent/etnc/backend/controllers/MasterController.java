package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.commons.RsqlFilterUtils;
import net.ent.etnc.backend.dtos.assemblers.MasterAssembler;
import net.ent.etnc.backend.dtos.requests.MasterRequest;
import net.ent.etnc.backend.dtos.responses.MasterResponse;
import net.ent.etnc.backend.models.entities.Master;
import net.ent.etnc.backend.services.MasterService;
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
 * Endpoint REST pour la gestion des masters ({@code /api/v1/masters/}).
 */
@RestController
@RequestMapping("/api/v1/masters/")
public class MasterController {

    private static final Map<String, String> FILTERABLE = Map.of(
            "nom", "nom"
    );

    private final MasterService masterService;
    private final MasterAssembler assembler;
    private final RsqlFilterUtils rsqlFilterUtils;

    @Autowired
    public MasterController(MasterService masterService, MasterAssembler masterAssembler, RsqlFilterUtils rsqlFilterUtils) {
        this.masterService = masterService;
        this.assembler = masterAssembler;
        this.rsqlFilterUtils = rsqlFilterUtils;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<MasterResponse>> getAll(
            @RequestParam(required = false) String filter,
            @PageableDefault(size = 20) Pageable pageable) {
        Specification<Master> spec = rsqlFilterUtils.build(filter, FILTERABLE);
        return ResponseEntity.ok(masterService.findAll(spec, pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<MasterResponse> getById(@PathVariable Long id) {
        return masterService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toResponse(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<MasterResponse> post(@RequestBody MasterRequest masterRequest) {
        return ResponseEntity.ok(assembler.toResponse(masterService.create(assembler.toEntity(masterRequest))));
    }
}
