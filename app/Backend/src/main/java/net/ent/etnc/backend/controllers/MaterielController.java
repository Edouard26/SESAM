package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.commons.JsonPatchUtils;
import net.ent.etnc.backend.commons.RsqlFilterUtils;
import net.ent.etnc.backend.dtos.assemblers.UniteCentraleAssembler;
import net.ent.etnc.backend.dtos.requests.MaterielRequest;
import net.ent.etnc.backend.dtos.responses.MaterielResponse;
import net.ent.etnc.backend.dtos.assemblers.MaterielAssembler;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.services.EcranService;
import net.ent.etnc.backend.services.MaterielService;
import net.ent.etnc.backend.services.UniteCentraleService;
import net.ent.etnc.backend.services.VideoProjecteurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    );

    private final MaterielService materielService;
    private final UniteCentraleService uniteCentraleService;
    private final EcranService ecranService;
    private final VideoProjecteurService videoProjecteurService;

    private final MaterielAssembler assembler;
    private final UniteCentraleAssembler uniteCentraleAssembler;

    private final RsqlFilterUtils rsqlFilterUtils;
    private final JsonPatchUtils jsonPatchUtils;

    @Autowired
    public MaterielController(MaterielService materielService, UniteCentraleService uniteCentraleService, EcranService ecranService, VideoProjecteurService videoProjecteurService, MaterielAssembler assembler, UniteCentraleAssembler uniteCentraleAssembler, RsqlFilterUtils rsqlFilterUtils, JsonPatchUtils jsonPatchUtils) {
        this.materielService = materielService;
        this.uniteCentraleService = uniteCentraleService;
        this.ecranService = ecranService;
        this.videoProjecteurService = videoProjecteurService;
        this.assembler = assembler;
        this.uniteCentraleAssembler = uniteCentraleAssembler;
        this.rsqlFilterUtils = rsqlFilterUtils;
        this.jsonPatchUtils = jsonPatchUtils;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<MaterielResponse>> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(materielService.findAll(pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<MaterielResponse> getById(@PathVariable Long id) {
        return materielService.findById(id)
                .map(m -> ResponseEntity.ok(assembler.toResponse(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<MaterielResponse> post(@RequestBody MaterielRequest materielRequest) {

        switch (materielRequest.getTypeMateriel()) {
            case "UniteCentrale" -> {
                return ResponseEntity.ok(assembler.toResponse(materielService.create(assembler.toUniteCentrale(materielRequest))));
            }
            case "Ecran" -> {
                return ResponseEntity.ok(assembler.toResponse(materielService.create(assembler.toEcran(materielRequest))));
            }
            case "VideoProjecteur" -> {
                return ResponseEntity.ok(assembler.toResponse(materielService.create(assembler.toVideoProjecteur(materielRequest))));
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
        MaterielRequest patched = jsonPatchUtils.apply(patch, assembler.toRequest(existing.get()), MaterielRequest.class, PATCHABLE);

        Materiel entity;
        switch (patched.getTypeMateriel()){
            case "UniteCentrale" -> {
                entity = assembler.toUniteCentrale(patched);
            }
            case "Ecran" -> {
                entity = assembler.toEcran(patched);
            }
            case "VideoProjecteur" -> {
                entity = assembler.toVideoProjecteur(patched);
            }
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }
        entity.setId(id);
        entity = materielService.update(entity);
        return ResponseEntity.ok(assembler.toResponse(entity));
    }

}