package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.commons.RsqlFilterUtils;
import net.ent.etnc.backend.dtos.requests.SalleRequest;
import net.ent.etnc.backend.dtos.responses.SalleResponse;
import net.ent.etnc.backend.dtos.assemblers.SalleAssembler;
import net.ent.etnc.backend.models.entities.Batiment;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.services.BatimentService;
import net.ent.etnc.backend.services.SalleService;
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
 * Endpoint REST pour la gestion des salles ({@code /api/v1/rooms/}).
 */
@RestController
@RequestMapping("/api/v1/rooms/")
public class SalleController {

    private static final Map<String, String> FILTERABLE = Map.of(
            "nom", "nom",
            "type", "typeSalle",
            "etat", "etat"
    );

    private final SalleService salleService;
    private final BatimentService batimentService;
    private final SalleAssembler assembler;

    private final RsqlFilterUtils rsqlFilterUtils;

    @Autowired
    public SalleController(SalleService salleService, BatimentService batimentService, SalleAssembler assembler, RsqlFilterUtils rsqlFilterUtils) {
        this.salleService = salleService;
        this.batimentService = batimentService;
        this.assembler = assembler;
        this.rsqlFilterUtils = rsqlFilterUtils;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<SalleResponse>> getAll(
            @RequestParam(required = false) String filter,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Specification<Salle> spec = this.rsqlFilterUtils.build(filter, FILTERABLE);
        return ResponseEntity.ok(salleService.findAll(spec, pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<SalleResponse> getById(@PathVariable Long id) {
        return salleService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toResponse(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<SalleResponse> post(@RequestBody SalleRequest salleRequest) {
        Batiment batiment = batimentService.findByNom(salleRequest.getNomBatiment());
        Salle salle = salleService.create(assembler.toEntity(salleRequest));

        batiment.getSalles().add(salle);
        batimentService.update(batiment);

        return ResponseEntity.ok(assembler.toResponse(salle));
    }


}