package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.dtos.requests.StageRequest;
import net.ent.etnc.backend.dtos.responses.StageResponse;
import net.ent.etnc.backend.dtos.assemblers.StageAssembler;
import net.ent.etnc.backend.services.StageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * TODO
 */
@RestController
@RequestMapping("/api/v1/stages/")
public class StageController {

    private final StageService stageService;
    private final StageAssembler assembler;

    @Autowired
    public StageController(StageService stageService, StageAssembler assembler) {
        this.stageService = stageService;
        this.assembler = assembler;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<StageResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(stageService.findAll(pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<StageResponse> getById(@PathVariable Long id) {
        return stageService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toResponse(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<StageResponse> post(@RequestBody StageRequest stageRequest) {
        return ResponseEntity.ok(assembler.toResponse(stageService.create(assembler.toEntity(stageRequest))));
    }
}