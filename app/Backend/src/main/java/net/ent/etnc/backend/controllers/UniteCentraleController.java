package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.dtos.UniteCentraleDto;
import net.ent.etnc.backend.dtos.assemblers.UniteCentraleAssembler;
import net.ent.etnc.backend.services.UniteCentraleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint REST pour la gestion des unités centrales ({@code /api/v1/UCs/}).
 */
@RestController
@RequestMapping("/api/v1/UCs/")
public class UniteCentraleController {

    private final UniteCentraleService uniteCentraleService;
    private final UniteCentraleAssembler assembler;

    @Autowired
    public UniteCentraleController(UniteCentraleService uniteCentraleService, UniteCentraleAssembler assembler) {
        this.uniteCentraleService = uniteCentraleService;
        this.assembler = assembler;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<UniteCentraleDto>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(uniteCentraleService.findAll(pageable).map(assembler::toDto));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<UniteCentraleDto> getById(@PathVariable Long id) {
        return uniteCentraleService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toDto(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<UniteCentraleDto> post(@RequestBody UniteCentraleDto uniteCentraleDto) {
        return ResponseEntity.ok(assembler.toDto(uniteCentraleService.create(assembler.toEntity(uniteCentraleDto))));
    }

    @PutMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<UniteCentraleDto> put(@PathVariable Long id, @RequestBody UniteCentraleDto uniteCentraleDto) {
        if (!uniteCentraleService.existsById(id)) return ResponseEntity.notFound().build();
        uniteCentraleDto.setId(id);

        return ResponseEntity.ok(assembler.toDto(uniteCentraleService.update(assembler.toEntity(uniteCentraleDto))));
    }

    @DeleteMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!uniteCentraleService.existsById(id)) return ResponseEntity.notFound().build();

        uniteCentraleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}