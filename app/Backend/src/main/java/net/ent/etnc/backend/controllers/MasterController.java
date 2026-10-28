package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.dtos.assemblers.MasterAssembler;
import net.ent.etnc.backend.dtos.requests.MasterRequest;
import net.ent.etnc.backend.dtos.responses.MasterResponse;
import net.ent.etnc.backend.services.MasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/masters/")
public class MasterController {

    private final MasterService masterService;
    private final MasterAssembler assembler;

    @Autowired
    public MasterController(MasterService masterService, MasterAssembler masterAssembler) {
        this.masterService = masterService;
        this.assembler = masterAssembler;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<MasterResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(masterService.findAll(pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<MasterResponse> getById(@PathVariable Long id) {
        return masterService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toResponse(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<MasterResponse> post(@RequestBody MasterRequest masterRequest) {
        return ResponseEntity.ok(assembler.toResponse(masterService.create(assembler.toEntity(masterRequest))));
    }
}
