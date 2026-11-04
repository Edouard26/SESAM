package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.commons.JsonPatchUtils;
import net.ent.etnc.backend.commons.RsqlFilterUtils;
import net.ent.etnc.backend.dtos.requests.AlerteResolutionRequest;
import net.ent.etnc.backend.dtos.requests.AlerteSignalementRequest;
import net.ent.etnc.backend.dtos.responses.AlerteResponse;
import net.ent.etnc.backend.dtos.assemblers.AlerteAssembler;
import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.enums.Role;
import net.ent.etnc.backend.services.AlerteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Endpoint REST pour la gestion des alertes ({@code /api/v1/alerts/}).
 */
@RestController
@RequestMapping("/api/v1/alerts/")
public class AlerteController {

    private static final Set<String> PATCHABLE = Set.of(
            "resolveurId",
            "dateResolution",
            "statut"
    );

    private static final Map<String, String> FILTERABLE = Map.of(
            "statut", "statut"
    );

    private final AlerteService alerteService;
    private final AlerteAssembler assembler;

    private final JsonPatchUtils jsonPatchUtils;
    private final RsqlFilterUtils rsqlFilterUtils;

    @Autowired
    public AlerteController(AlerteService alerteService, AlerteAssembler assembler, JsonPatchUtils jsonPatchUtils, RsqlFilterUtils rsqlFilterUtils) {
        this.alerteService = alerteService;
        this.assembler = assembler;
        this.jsonPatchUtils = jsonPatchUtils;
        this.rsqlFilterUtils = rsqlFilterUtils;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STAGIAIRE', 'GESTIONNAIRE')")
    public ResponseEntity<Page<AlerteResponse>> getAll(
            Authentication authentication,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Long salleId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        User currentUser = (User) authentication.getPrincipal();

        // si role Stagiaire -> renvoie ses alertes
        if(currentUser.getRole() == Role.STAGIAIRE){
            return ResponseEntity.ok(alerteService.findAllByUserId(pageable, currentUser.getId()).map(assembler::toResponse));
        }

        // si role Formateur -> renvoie les alertes de la salle
        if(currentUser.getRole() == Role.FORMATEUR) {
            if (salleId != null) {
                return ResponseEntity.ok(alerteService.findAllBySalleId(pageable, salleId).map(assembler::toResponse));
            }
            else {
                return ResponseEntity.badRequest().build();
            }
        }

        // si role Gestionnaire -> renvoie tous les alertes

        // si il y a salleId -> renvoie tous les alertes de la salle
        if (salleId != null) {
            return ResponseEntity.ok(alerteService.findAllBySalleId(pageable, salleId).map(assembler::toResponse));
        }

        // sinon -> renvoie tous les alertes
        Specification<Alerte> spec = this.rsqlFilterUtils.build(filter, FILTERABLE);
        return ResponseEntity.ok(alerteService.findAll(spec, pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasAnyRole('STAGIAIRE', 'GESTIONNAIRE')")
    public ResponseEntity<AlerteResponse> getById(Authentication authentication, @PathVariable Long id) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(assembler.toResponse(alerteService.findById(currentUser, id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('STAGIAIRE')")
    public ResponseEntity<AlerteResponse> post(@RequestBody AlerteSignalementRequest alerteSignalementRequest) {
        return ResponseEntity.ok(assembler.toResponse(alerteService.create(assembler.toEntity(alerteSignalementRequest))));
    }

    @PatchMapping(value = "{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<AlerteResponse> patch(@PathVariable Long id, @RequestBody String patch){
        Optional<Alerte> existing = alerteService.findById(id);
        if(existing.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        AlerteResolutionRequest patched = jsonPatchUtils.apply(patch, assembler.toRequest(existing.get()), AlerteResolutionRequest.class, PATCHABLE);
        Alerte entity = assembler.toEntityPatch(existing.get(), patched);
        entity.setId(id);
        entity = alerteService.update(entity);

        return ResponseEntity.ok(assembler.toResponse(entity));
    }

}