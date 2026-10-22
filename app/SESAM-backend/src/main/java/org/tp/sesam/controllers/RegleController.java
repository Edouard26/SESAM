package org.tp.sesam.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.User;
import org.tp.sesam.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static org.tp.sesam.controllers.ApiControllerSupport.actorId;

/** Couche HTTP : validation, identité connectée, délégation au service et code de réponse. */
@RestController
@RequestMapping("/api/v1/rules")
@PreAuthorize("hasRole('FORMATEUR')")
public class RegleController {
    private final RegleService service;
    public RegleController(RegleService service) {
        this.service = service;
    }


    /** L'identité connectée détermine le propriétaire des règles de salle. */
    @GetMapping("/rooms/{roomId}")
    public List<RegleResponse> room(@PathVariable("roomId") @Positive Long roomId, @AuthenticationPrincipal User user) {
        return service.listerSalle(roomId, actorId(user));
    }
    @PatchMapping("/rooms/{roomId}/{processId}")
    public RegleResponse patchRoom(@PathVariable("roomId") @Positive Long roomId,
            @PathVariable("processId") @Positive Long processId,
            @Valid @RequestBody ReglePatch dto, @AuthenticationPrincipal User user) {
        return service.modifierSalle(roomId, processId, dto, actorId(user));
    }
    @DeleteMapping("/rooms/{roomId}/{processId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("roomId") @Positive Long roomId,
            @PathVariable("processId") @Positive Long processId, @AuthenticationPrincipal User user) {
        service.supprimerSalle(roomId, processId, actorId(user));
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteAllRoom(@PathVariable("roomId") @Positive Long roomId, @AuthenticationPrincipal User user) {
        service.supprimerToutesSalle(roomId, actorId(user));
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/stages/{stageId}")
    @PreAuthorize("hasAnyRole('FORMATEUR','ADMINISTRATEUR')")
    public List<RegleResponse> stage(@PathVariable("stageId") @Positive Long stageId, @AuthenticationPrincipal User user) {
        return service.listerStage(stageId, actorId(user));
    }
    @PatchMapping("/stages/{stageId}/{processId}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public RegleResponse patchStage(@PathVariable("stageId") @Positive Long stageId,
            @PathVariable("processId") @Positive Long processId,
            @Valid @RequestBody ReglePatch dto, @AuthenticationPrincipal User user) {
        return service.modifierStage(stageId, processId, dto, actorId(user));
    }
    @DeleteMapping("/stages/{stageId}/{processId}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Void> deleteStage(@PathVariable("stageId") @Positive Long stageId,
            @PathVariable("processId") @Positive Long processId, @AuthenticationPrincipal User user) {
        service.supprimerStage(stageId, processId, actorId(user));
        return ResponseEntity.noContent().build();
    }
}
