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
@RequestMapping("/api/v1/stages")
@PreAuthorize("hasAnyRole('GESTIONNAIRE')")
public class StageController {
    private final StageService service;
    private final StageDtoAssembler assembler;
    public StageController(StageService service, StageDtoAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }


    @GetMapping
    public Page<StageResponseDto> findAll(@Valid @ModelAttribute StageFilter filtre, Pageable pageable, @AuthenticationPrincipal User user) {
        return service.rechercher(filtre, pageable, actorId(user)).map(assembler::toDto);
    }
    @GetMapping("/{stageId}")
    public StageResponseDto findById(@PathVariable("stageId") @Positive Long stageId, @AuthenticationPrincipal User user) {
        return assembler.toDto(service.consulter(stageId, actorId(user)));
    }
    @PatchMapping("/{stageId}")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public StageResponseDto patch(@PathVariable("stageId") @Positive Long stageId,
            @Valid @RequestBody StagePatch dto, @AuthenticationPrincipal User user) {
        return assembler.toDto(service.modifier(stageId, dto, actorId(user)));
    }
    @PostMapping("/{stageId}/users")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Void> assignUsers(@PathVariable("stageId") @Positive Long stageId,
            @Valid @RequestBody AffectationUsers dto, @AuthenticationPrincipal User user) {
        service.affecterUsers(stageId, dto, actorId(user));
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{stageId}/closure")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public StageResponseDto close(@PathVariable("stageId") @Positive Long stageId, @AuthenticationPrincipal User user) {
        return assembler.toDto(service.cloturer(stageId, actorId(user)));
    }
    @PostMapping("/{stageId}/notifications/sagaie/retry")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<NotificationResponse> retry(@PathVariable("stageId") @Positive Long stageId, @AuthenticationPrincipal User user) {
        return ResponseEntity.accepted().body(service.relancerSagaie(stageId, actorId(user)));
    }
}
