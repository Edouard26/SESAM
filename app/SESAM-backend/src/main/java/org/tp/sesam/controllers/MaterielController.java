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
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import static org.tp.sesam.controllers.ApiControllerSupport.actorId;

/** Couche HTTP : validation, identité connectée, délégation au service et code de réponse. */
@RestController
@RequestMapping("/api/v1/materials")
@PreAuthorize("isAuthenticated()")
public class MaterielController {
    private final MaterielService service;
    private final MaterielDtoAssembler assembler;
    private final ReinstallationService reinstallations;
    public MaterielController(MaterielService service, MaterielDtoAssembler assembler, ReinstallationService reinstallations) {
        this.service = service;
        this.assembler = assembler;
        this.reinstallations = reinstallations;
    }


    @GetMapping
    public Page<MaterielResponseDto> findAll(@Valid @ModelAttribute MaterielFilter filtre,
            Pageable pageable, @AuthenticationPrincipal User user) {
        return service.rechercher(filtre, pageable, actorId(user)).map(assembler::toDto);
    }
    @GetMapping("/{materialId}")
    public MaterielResponseDto findById(@PathVariable("materialId") @Positive Long materialId, @AuthenticationPrincipal User user) {
        return assembler.toDto(service.consulter(materialId, actorId(user)));
    }
    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<MaterielResponseDto> create(@Valid @RequestBody MaterielCreation dto, @AuthenticationPrincipal User user) {
        var saved = service.creer(dto, actorId(user));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(assembler.toDto(saved));
    }
    /** Le service fusionne les champs présents et vérifie la version avant enregistrement. */
    @PatchMapping("/{materialId}")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public MaterielResponseDto patch(@PathVariable("materialId") @Positive Long materialId,
            @Valid @RequestBody MaterielPatch dto, @AuthenticationPrincipal User user) {
        return assembler.toDto(service.modifier(materialId, dto, actorId(user)));
    }
    @PostMapping("/assignments")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Void> assign(@Valid @RequestBody AffectationMateriels dto, @AuthenticationPrincipal User user) {
        service.affecter(dto, actorId(user));
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{materialId}/reinstallations")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<ReinstallationResponse> reinstall(@PathVariable("materialId") @Positive Long materialId,
            @AuthenticationPrincipal User user) {
        var result = reinstallations.lancerMateriel(materialId, actorId(user));
        return ResponseEntity.accepted().header(HttpHeaders.LOCATION,
                "/api/v1/reinstallations/" + result.id()).body(result);
    }
}
