package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.commons.JsonPatchUtils;
import net.ent.etnc.backend.commons.RsqlFilterUtils;
import net.ent.etnc.backend.dtos.assemblers.UniteCentraleAssembler;
import net.ent.etnc.backend.dtos.requests.MaterielAffectationRequest;
import net.ent.etnc.backend.dtos.requests.MaterielImportRequest;
import net.ent.etnc.backend.dtos.requests.MaterielRequest;
import net.ent.etnc.backend.dtos.responses.MaterielImportResponse;
import net.ent.etnc.backend.dtos.responses.MaterielResponse;
import net.ent.etnc.backend.dtos.assemblers.MaterielAssembler;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.UniteCentrale;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.enums.EtatMateriel;
import net.ent.etnc.backend.models.enums.Role;
import net.ent.etnc.backend.models.enums.TypeMateriel;
import net.ent.etnc.backend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    private final SalleService salleService;

    private final MaterielAssembler materielAssembler;

    private final RsqlFilterUtils rsqlFilterUtils;
    private final JsonPatchUtils jsonPatchUtils;

    @Autowired
    public MaterielController(MaterielService materielService, MaterielAssembler materielAssembler, RsqlFilterUtils rsqlFilterUtils, JsonPatchUtils jsonPatchUtils, SalleService salleService) {
        this.materielService = materielService;
        this.materielAssembler = materielAssembler;
        this.rsqlFilterUtils = rsqlFilterUtils;
        this.jsonPatchUtils = jsonPatchUtils;
        this.salleService = salleService;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE') || hasRole('STAGIAIRE')")
    public ResponseEntity<Page<MaterielResponse>> getAll(
            Authentication authentication,
            @RequestParam(required = false) String filter,
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) Long salleId,
            @RequestParam(required = false) Long excludeSalleId
    ) {
        User curentUser = (User) authentication.getPrincipal();

        // Si c'est un stagiaire, on renvoie les matériels affectés au stagiaire.
        if(curentUser.getRole() == Role.STAGIAIRE){
            return ResponseEntity.ok(materielService.findAllMaterielAffectToStagiaire(curentUser, pageable).map(materielAssembler::toResponse));
        }

        Specification<Materiel> spec = this.rsqlFilterUtils.build(filter, FILTERABLE);

        // Si c'est un formateur, on renvoie les matériels affectés à la salle dans laquel il est.
        if(curentUser.getRole() == Role.FORMATEUR){
            return ResponseEntity.ok(materielService.findAllBySalleId(spec, pageable, salleId).map(materielAssembler::toResponse));
        }

        // Si c'est c'est un gestionnaire ou un administrateur, on renvoie les matériels du parc informatique.
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
            case TypeMateriel.UC -> {
                return ResponseEntity.ok(materielAssembler.toResponse(materielService.create(materielAssembler.toUniteCentrale(materielRequest))));
            }
            case TypeMateriel.ECRAN -> {
                return ResponseEntity.ok(materielAssembler.toResponse(materielService.create(materielAssembler.toEcran(materielRequest))));
            }
            case TypeMateriel.VIDEO_PROJECTEUR -> {
                return ResponseEntity.ok(materielAssembler.toResponse(materielService.create(materielAssembler.toVideoProjecteur(materielRequest))));
            }
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }

    }

    @PostMapping("import/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<MaterielImportResponse> postImport(@RequestBody MaterielImportRequest materielImportRequest){
        Optional<Salle> optionalSalle = salleService.findById(materielImportRequest.getSalleId());
        if(optionalSalle.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        List<Materiel> materiels = new ArrayList<>();
        for(MaterielRequest materielRequest : materielImportRequest.getMateriels()){
            // TODO code dupliquer, créer une fonction
            Materiel materiel;
            switch (materielRequest.getTypeMateriel()) {
                case TypeMateriel.UC -> {
                    materiel = materielAssembler.toUniteCentrale(materielRequest);
                }
                case TypeMateriel.ECRAN -> {
                    materiel = materielAssembler.toEcran(materielRequest);
                }
                case TypeMateriel.VIDEO_PROJECTEUR -> {
                    materiel = materielAssembler.toVideoProjecteur(materielRequest);
                }
                default -> {
                    return ResponseEntity.badRequest().build();
                }
            }
            materiel.setEtat(EtatMateriel.EN_ETAT);
            if(!materielService.existsByNumeroSerie(materiel.getNumeroSerie())) {
                materielService.create(materiel);
                optionalSalle.get().getMateriels().add(materiel);
                materiels.add(materiel);
            }
        }
        salleService.update(optionalSalle.get());

        return ResponseEntity.ok(materielAssembler.toResponseImport(optionalSalle.get().getId(), materiels));
    }

    @PatchMapping(value = "{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<MaterielResponse> patch(@PathVariable Long id, @RequestBody String patch) {
        Optional<Materiel> existing = materielService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        MaterielRequest patched = jsonPatchUtils.apply(patch, materielAssembler.toRequest(existing.get()), MaterielRequest.class, PATCHABLE);

        Materiel entity;
        switch (patched.getTypeMateriel()){
            case TypeMateriel.UC -> {
                entity = materielAssembler.toUniteCentrale(patched);
            }
            case TypeMateriel.ECRAN -> {
                entity = materielAssembler.toEcran(patched);
            }
            case TypeMateriel.VIDEO_PROJECTEUR -> {
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

        optionalSalle.get().getMateriels().addAll(materielService.findAllByIdIn(materielAffectationRequest.getMaterielsId()));
        salleService.update(optionalSalle.get());

        return ResponseEntity.ok().build();
    }

}