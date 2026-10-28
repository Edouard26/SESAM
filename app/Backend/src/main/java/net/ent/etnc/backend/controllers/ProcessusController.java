package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.dtos.assemblers.ProcessusAssembler;
import net.ent.etnc.backend.dtos.requests.ProcessusRequest;
import net.ent.etnc.backend.dtos.responses.ProcessusResponse;
import net.ent.etnc.backend.services.ProcessusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/process/")
public class ProcessusController {

    private final ProcessusService processusService;
    private final ProcessusAssembler assembler;

    @Autowired
    public ProcessusController(ProcessusService processusService, ProcessusAssembler assembler) {
        this.processusService = processusService;
        this.assembler = assembler;
    }

    @GetMapping
    @PreAuthorize("hasRole('FORMATEUR') || hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<ProcessusResponse>> getAll(@PageableDefault(size = 20) Pageable pageable){
        return ResponseEntity.ok(processusService.findAll(pageable).map(assembler::toResponse));
    }

    @GetMapping("{id}/")
    @PreAuthorize("hasRole('FORMATEUR') || hasRole('GESTIONNAIRE')")
    public ResponseEntity<ProcessusResponse> getById(@PathVariable Long id) {
        return processusService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toResponse(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('FORMATEUR') || hasRole('GESTIONNAIRE')")
    public ResponseEntity<ProcessusResponse> post(@RequestBody ProcessusRequest processusRequest) {
        return ResponseEntity.ok(assembler.toResponse(processusService.create(assembler.toEntity(processusRequest))));
    }
}
