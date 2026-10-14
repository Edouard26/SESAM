package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.dtos.EcranDto;
import net.ent.etnc.backend.dtos.assemblers.EcranAssembler;
import net.ent.etnc.backend.services.EcranService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint REST pour la gestion des écrans ({@code /api/v1/screens/}).
 */
@RestController
@RequestMapping("/api/v1/screens/")
public class EcranController {

    private final EcranService ecranService;
    private final EcranAssembler assembler;

    @Autowired
    public EcranController(EcranService ecranService, EcranAssembler assembler) {
        this.ecranService = ecranService;
        this.assembler = assembler;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<EcranDto>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ecranService.findAll(pageable).map(assembler::toDto));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<EcranDto> getById(@PathVariable Long id) {
        return ecranService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toDto(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<EcranDto> post(@RequestBody EcranDto ecranDto) {
        return ResponseEntity.ok(assembler.toDto(ecranService.create(assembler.toEntity(ecranDto))));
    }

    @PutMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<EcranDto> put(@PathVariable Long id, @RequestBody EcranDto ecranDto) {
        if (!ecranService.existsById(id)) return ResponseEntity.notFound().build();
        ecranDto.setId(id);

        return ResponseEntity.ok(assembler.toDto(ecranService.update(assembler.toEntity(ecranDto))));
    }

    @DeleteMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!ecranService.existsById(id)) return ResponseEntity.notFound().build();

        ecranService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}