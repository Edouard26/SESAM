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
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public class UserController {
    private final UserService service;
    public UserController(UserService service) {
        this.service = service;
    }


    @GetMapping
    public Page<UserResponse> findAll(@Valid @ModelAttribute UserFilter filtre, Pageable pageable, @AuthenticationPrincipal User user) {
        return service.rechercher(filtre, pageable, actorId(user));
    }
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreation dto, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(201).body(service.creer(dto, actorId(user)));
    }
    @PatchMapping("/{userId}")
    public UserResponse patch(@PathVariable("userId") @Positive Long userId,
            @Valid @RequestBody UserPatch dto, @AuthenticationPrincipal User user) {
        return service.modifier(userId, dto, actorId(user));
    }
}
