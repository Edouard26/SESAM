package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.dtos.requests.SalleRequest;
import net.ent.etnc.backend.dtos.responses.SalleResponse;
import net.ent.etnc.backend.dtos.assemblers.SalleAssembler;
import net.ent.etnc.backend.models.entities.Batiment;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.services.BatimentService;
import net.ent.etnc.backend.services.SalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint REST pour la gestion des salles ({@code /api/v1/rooms/}).
 */
@RestController
@RequestMapping("/api/v1/rooms/")
public class SalleController {

    private final SalleService salleService;
    private final BatimentService batimentService;
    private final SalleAssembler assembler;

    @Autowired
    public SalleController(SalleService salleService, BatimentService batimentService, SalleAssembler assembler) {
        this.salleService = salleService;
        this.batimentService = batimentService;
        this.assembler = assembler;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<SalleResponse>> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(salleService.findAll(pageable).map(assembler::toResponse));
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