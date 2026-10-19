package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.commons.JsonPatchUtils;
import net.ent.etnc.backend.commons.RsqlFilterUtils;
import net.ent.etnc.backend.dtos.assemblers.UniteCentraleAssembler;
import net.ent.etnc.backend.dtos.requests.MaterielAffectationRequest;
import net.ent.etnc.backend.dtos.requests.MaterielRequest;
import net.ent.etnc.backend.dtos.responses.MaterielResponse;
import net.ent.etnc.backend.dtos.assemblers.MaterielAssembler;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Endpoint REST pour la gestion des matériels ({@code /api/v1/materials/}).
 */
@RestController
@RequestMapping("/api/v1/materials/")
public class MaterielController {

    private static final Set<String> PATCHABLE = Set.of(
            "nom"
    );

    private static final Map<String, String> FILTERABLE = Map.of(
            "numeroSerie", "numeroSerie",
            "marque", "marque",
            "modele", "modele",
            "dateAjout", "dateAjout",
            "dateFinGarantie", "dateFinGarantie",
            "etat", "etat",
            "type", "type"
    );

    private final MaterielService materielService;

    private final MaterielAssembler materielAssembler;

    private final RsqlFilterUtils rsqlFilterUtils;
    private final JsonPatchUtils jsonPatchUtils;
    private final SalleService salleService;

    @Autowired
    public MaterielController(MaterielService materielService, MaterielAssembler materielAssembler, RsqlFilterUtils rsqlFilterUtils, JsonPatchUtils jsonPatchUtils, SalleService salleService) {
        this.materielService = materielService;
        this.materielAssembler = materielAssembler;
        this.rsqlFilterUtils = rsqlFilterUtils;
        this.jsonPatchUtils = jsonPatchUtils;
        this.salleService = salleService;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<MaterielResponse>> getAll(
            @RequestParam(required = false) String filter,
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) Long salleId,
            @RequestParam(required = false) Long excludeSalleId
    ) {
        Specification<Materiel> spec = this.rsqlFilterUtils.build(filter, FILTERABLE);
        if(salleId != null){
            return ResponseEntity.ok(materielService.findAllBySalleId(spec, pageable, salleId).map(materielAssembler::toResponse));
        }
        if(excludeSalleId != null){
            return ResponseEntity.ok(materielService.findAllByExcludedSalleId(spec, pageable, excludeSalleId).map(materielAssembler::toResponse));
        }
        return ResponseEntity.ok(materielService.findAll(spec, pageable).map(materielAssembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<MaterielResponse> getById(@PathVariable Long id) {
        return materielService.findById(id)
                .map(m -> ResponseEntity.ok(materielAssembler.toResponse(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<MaterielResponse> post(@RequestBody MaterielRequest materielRequest) {

        switch (materielRequest.getTypeMateriel()) {
            case "UniteCentrale" -> {
                return ResponseEntity.ok(materielAssembler.toResponse(materielService.create(materielAssembler.toUniteCentrale(materielRequest))));
            }
            case "Ecran" -> {
                return ResponseEntity.ok(materielAssembler.toResponse(materielService.create(materielAssembler.toEcran(materielRequest))));
            }
            case "VideoProjecteur" -> {
                return ResponseEntity.ok(materielAssembler.toResponse(materielService.create(materielAssembler.toVideoProjecteur(materielRequest))));
            }
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }

    }

    @PatchMapping(value = "{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<MaterielResponse> patch(@PathVariable Long id, @RequestBody String patch) {
        Optional<Materiel> existing = materielService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        MaterielRequest patched = jsonPatchUtils.apply(patch, materielAssembler.toRequest(existing.get()), MaterielRequest.class, PATCHABLE);

        Materiel entity;
        switch (patched.getTypeMateriel()){
            case "UniteCentrale" -> {
                entity = materielAssembler.toUniteCentrale(patched);
            }
            case "Ecran" -> {
                entity = materielAssembler.toEcran(patched);
            }
            case "VideoProjecteur" -> {
                entity = materielAssembler.toVideoProjecteur(patched);
            }
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }
        entity.setId(id);
        entity = materielService.update(entity);
        return ResponseEntity.ok(materielAssembler.toResponse(entity));
    }

    /**
     * HTTP post pour l'affectation d'un ou plusieurs matériels à une nouvelle salle.
     * @param materielAffectationRequest
     * @return HTTP status 200
     */
    @PostMapping(value = "assignments/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Void> affectation(@RequestBody MaterielAffectationRequest materielAffectationRequest){
        Optional<Salle> optionalSalle = salleService.findById(materielAffectationRequest.getSalleId());
        if(optionalSalle.isEmpty()) return ResponseEntity.notFound().build();

        // TODO faire qu'une seul requete à la bdd
        for(Long materielID : materielAffectationRequest.getMaterielsId()){
            Optional<Materiel> optionalMateriel = materielService.findById(materielID);
            if(optionalMateriel.isPresent()){
                optionalSalle.get().getMateriels().add(optionalMateriel.get());
            }
        }
        salleService.update(optionalSalle.get());

        return ResponseEntity.ok().build();
    }

}