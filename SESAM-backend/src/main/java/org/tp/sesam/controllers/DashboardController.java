package org.tp.sesam.controllers;

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
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("isAuthenticated()")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) {
        this.service = service;
    }


    @GetMapping
    public DashboardResponse get(@AuthenticationPrincipal User user) {
        return service.consulter(actorId(user));
    }
}
