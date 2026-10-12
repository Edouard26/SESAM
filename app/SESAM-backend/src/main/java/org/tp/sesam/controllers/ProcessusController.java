package org.tp.sesam.controllers;

import jakarta.validation.constraints.Positive;
import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.User;
import org.tp.sesam.services.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static org.tp.sesam.controllers.ApiControllerSupport.actorId;

/** Couche HTTP : validation, identité connectée, délégation au service et code de réponse. */
@RestController
@RequestMapping("/api/v1/processes")
@PreAuthorize("hasAnyRole('FORMATEUR','ADMINISTRATEUR')")
public class ProcessusController {
    private final ProcessusService service;
    public ProcessusController(ProcessusService service) {
        this.service = service;
    }


    @GetMapping("/rooms/{roomId}")
    public List<ProcessusObserve> room(@PathVariable("roomId") @Positive Long roomId, @AuthenticationPrincipal User user) {
        return service.observerSalle(roomId, actorId(user));
    }
    @GetMapping("/materials/{materialId}")
    public List<ProcessusObserve> material(@PathVariable("materialId") @Positive Long materialId, @AuthenticationPrincipal User user) {
        return service.observerMateriel(materialId, actorId(user));
    }
}
