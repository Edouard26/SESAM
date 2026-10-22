package org.tp.sesam.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.User;
import org.tp.sesam.services.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import static org.tp.sesam.controllers.ApiControllerSupport.actorId;

/** Couche HTTP : validation, identité connectée, délégation au service et code de réponse. */
@RestController
@RequestMapping("/api/v1/alerts")
@PreAuthorize("isAuthenticated()")
public class AlerteController {
    private final AlerteService service;
    public AlerteController(AlerteService service) {
        this.service = service;
    }


    @GetMapping
    public Page<AlerteResponse> findAll(@ModelAttribute AlerteFilter filtre, Pageable pageable, @AuthenticationPrincipal User user) {
        return service.rechercher(filtre, pageable, actorId(user));
    }
    @PostMapping
    public ResponseEntity<AlerteResponse> create(@Valid @RequestBody AlerteCreation dto, @AuthenticationPrincipal User user) {
        // Pas de GET de détail dans le contrat : on renvoie directement la représentation créée.
        return ResponseEntity.status(201).body(service.creer(dto, actorId(user)));
    }
    @PatchMapping("/{alertId}")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public AlerteResponse patch(@PathVariable("alertId") @Positive Long alertId,
            @Valid @RequestBody AlertePatch dto, @AuthenticationPrincipal User user) {
        return service.modifier(alertId, dto, actorId(user));
    }
}
