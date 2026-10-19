package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.commons.JsonPatchUtils;
import net.ent.etnc.backend.commons.RsqlFilterUtils;
import net.ent.etnc.backend.dtos.requests.AlerteResolutionRequest;
import net.ent.etnc.backend.dtos.requests.AlerteSignalementRequest;
import net.ent.etnc.backend.dtos.responses.AlerteResponse;
import net.ent.etnc.backend.dtos.assemblers.AlerteAssembler;
import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.services.AlerteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    private final AlerteService alerteService;
    private final AlerteAssembler assembler;
    private final JsonPatchUtils jsonPatchUtils;

    @Autowired
    public AlerteController(AlerteService alerteService, AlerteAssembler assembler, JsonPatchUtils jsonPatchUtils) {
        this.alerteService = alerteService;
        this.assembler = assembler;
        this.jsonPatchUtils = jsonPatchUtils;
    }

    @GetMapping
    @PreAuthorize("hasRole('STAGIAIRE') || hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<AlerteResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(alerteService.findAll(pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('STAGIAIRE') || hasRole('GESTIONNAIRE')")
    public ResponseEntity<AlerteResponse> getById(@PathVariable Long id) {
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