package org.tp.sesam.controllers;

import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/imports")
@PreAuthorize("isAuthenticated()")
public class ImportController {
    private final TransfertService service;
    public ImportController(TransfertService service) {
        this.service = service;
    }


    @PostMapping("/{resource}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or (#p0 == 'materials' and hasRole('GESTIONNAIRE'))")
    public ImportResponse run(@PathVariable("resource") String resource,
            @Valid @RequestBody ImportRequest dto, @AuthenticationPrincipal User user) {
        return service.importer(ApiControllerSupport.resource(resource), dto, actorId(user));
    }
}
