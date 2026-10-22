package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.commons.JsonPatchUtils;
import net.ent.etnc.backend.commons.RsqlFilterUtils;
import net.ent.etnc.backend.dtos.requests.AlerteResolutionRequest;
import net.ent.etnc.backend.dtos.requests.AlerteSignalementRequest;
import net.ent.etnc.backend.dtos.responses.AlerteResponse;
import net.ent.etnc.backend.dtos.assemblers.AlerteAssembler;
import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.services.AlerteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    @PreAuthorize("hasRole('STAGIAIRE') || hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<AlerteResponse>> getAll(
            @RequestParam(required = false) String filter,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // TODO si role Gestionnaire -> renvoie tous les alertes
        //      si role Stagiaire -> renvoie ses alertes
        //      si role Formateur -> renvoie les alertes de la salle
        Specification<Alerte> spec = this.rsqlFilterUtils.build(filter, FILTERABLE);
        return ResponseEntity.ok(alerteService.findAll(spec, pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('STAGIAIRE') || hasRole('GESTIONNAIRE')")
    public ResponseEntity<AlerteResponse> getById(@PathVariable Long id) {
        // TODO vérifier si il a le droit d'accès à cette alerte
        return alerteService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toResponse(a)))
                .orElse(ResponseEntity.notFound().build());
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