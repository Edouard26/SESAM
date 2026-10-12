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
@RequestMapping("/api/v1/rooms")
@PreAuthorize("isAuthenticated()")
public class SalleController {
    private final SalleService service;
    private final SalleDtoAssembler assembler;
    private final ReinstallationService reinstallations;
    public SalleController(SalleService service, SalleDtoAssembler assembler, ReinstallationService reinstallations) {
        this.service = service;
        this.assembler = assembler;
        this.reinstallations = reinstallations;
    }


    /** Les filtres HTTP sont regroupés dans un objet validé, page/size/sort dans Pageable. */
    @GetMapping
    public Page<SalleResponseDto> findAll(@Valid @ModelAttribute SalleFilter filtre,
            Pageable pageable, @AuthenticationPrincipal User user) {
        return service.rechercher(filtre, pageable, actorId(user)).map(assembler::toDto);
    }
    @GetMapping("/{roomId}")
    public SalleResponseDto findById(@PathVariable("roomId") @Positive Long roomId, @AuthenticationPrincipal User user) {
        return assembler.toDto(service.consulter(roomId, actorId(user)));
    }
    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<SalleResponseDto> create(@Valid @RequestBody SalleRequestDto dto) {
        var saved = service.create(assembler.toEntity(dto));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(assembler.toDto(saved));
    }
    @PostMapping("/{roomId}/reinstallations")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<ReinstallationResponse> reinstall(@PathVariable("roomId") @Positive Long roomId,
            @AuthenticationPrincipal User user) {
        var result = reinstallations.lancerSalle(roomId, actorId(user));
        return ResponseEntity.accepted().header(HttpHeaders.LOCATION,
                "/api/v1/reinstallations/" + result.id()).body(result);
    }
}
