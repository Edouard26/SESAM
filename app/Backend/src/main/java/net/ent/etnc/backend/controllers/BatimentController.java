package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.commons.JsonPatchUtils;
import net.ent.etnc.backend.dtos.responses.BatimentResponse;
import net.ent.etnc.backend.dtos.assemblers.BatimentAssembler;
import net.ent.etnc.backend.models.entities.Batiment;
import net.ent.etnc.backend.services.BatimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

/**
 * Endpoint REST pour la gestion des bâtiments ({@code /api/v1/buildings/}).
 */
@RestController
@RequestMapping("/api/v1/buildings/")
public class BatimentController {

    private static final Set<String> PATCHABLE = Set.of(
        "nom"
    );

    private final BatimentService batimentService;
    private final BatimentAssembler assembler;

    private final JsonPatchUtils jsonPatchUtils;

    @Autowired
    public BatimentController(BatimentService batimentService, BatimentAssembler assembler, JsonPatchUtils jsonPatchUtils) {
        this.batimentService = batimentService;
        this.assembler = assembler;
        this.jsonPatchUtils = jsonPatchUtils;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<BatimentResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(batimentService.findAll(pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<BatimentResponse> getById(@PathVariable Long id) {
        return batimentService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toResponse(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<BatimentResponse> post(@RequestBody BatimentResponse batimentResponse) {// TODO changer en batimentRequest
        return ResponseEntity.ok(assembler.toResponse(batimentService.create(assembler.toEntity(batimentResponse))));
    }

    @PutMapping("{id}/")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<BatimentResponse> put(@PathVariable Long id, @RequestBody BatimentResponse batimentResponse) {
        if (!batimentService.existsById(id)) return ResponseEntity.notFound().build();
        batimentResponse.setId(id);

        return ResponseEntity.ok(assembler.toResponse(batimentService.update(assembler.toEntity(batimentResponse))));
    }

    @PatchMapping(value = "{id}/")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<BatimentResponse> patch(@PathVariable Long id, @RequestBody String patch) {
        Optional<Batiment> existing = batimentService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        BatimentResponse patched = jsonPatchUtils.apply(patch,assembler.toResponse(existing.get()), BatimentResponse.class, PATCHABLE);
        Batiment entity = assembler.toEntity(patched);
        entity.setId(id);
        entity = batimentService.update(entity);
        return ResponseEntity.ok(assembler.toResponse(entity));
    }

    @DeleteMapping("{id}/")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!batimentService.existsById(id)) return ResponseEntity.notFound().build();

        batimentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}