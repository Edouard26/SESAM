package org.tp.sesam.controllers;

import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.User;
import org.tp.sesam.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import static org.tp.sesam.controllers.ApiControllerSupport.actorId;

/** Couche HTTP : validation, identité connectée, délégation au service et code de réponse. */
@RestController
@RequestMapping("/api/v1/exports")
@PreAuthorize("isAuthenticated()")
public class ExportController {
    private final TransfertService service;
    public ExportController(TransfertService service) {
        this.service = service;
    }


    @GetMapping("/{resource}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or (#p0 == 'materials' and hasRole('GESTIONNAIRE'))")
    public ResponseEntity<byte[]> run(@PathVariable("resource") String resource,
            @RequestParam org.springframework.util.MultiValueMap<String,String> filtres, @AuthenticationPrincipal User user) {
        var file = service.exporter(ApiControllerSupport.resource(resource), filtres, actorId(user));
        // Le service doit valider les filtres propres à la ressource et exclure tout secret.
        String disposition = ContentDisposition.attachment()
                .filename(file.filename(), java.nio.charset.StandardCharsets.UTF_8).build().toString();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(file.content());
    }
}
