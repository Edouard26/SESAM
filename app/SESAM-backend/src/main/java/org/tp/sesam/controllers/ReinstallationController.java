package org.tp.sesam.controllers;

import jakarta.validation.constraints.Positive;
import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.User;
import org.tp.sesam.services.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import static org.tp.sesam.controllers.ApiControllerSupport.actorId;

/** Couche HTTP : validation, identité connectée, délégation au service et code de réponse. */
@RestController
@RequestMapping("/api/v1/reinstallations")
@PreAuthorize("hasRole('GESTIONNAIRE')")
public class ReinstallationController {
    private final ReinstallationService service;
    public ReinstallationController(ReinstallationService service) {
        this.service = service;
    }


    @GetMapping("/{reinstallationId}")
    public ReinstallationResponse get(@PathVariable("reinstallationId") @Positive Long reinstallationId, @AuthenticationPrincipal User user) {
        return service.consulter(reinstallationId, actorId(user));
    }
}
