package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.commons.JsonPatchUtils;
import net.ent.etnc.backend.commons.RsqlFilterUtils;
import net.ent.etnc.backend.dtos.requests.StageImportRequest;
import net.ent.etnc.backend.dtos.requests.StageRequest;
import net.ent.etnc.backend.dtos.responses.StageImportResponse;
import net.ent.etnc.backend.dtos.responses.StageResponse;
import net.ent.etnc.backend.dtos.assemblers.StageAssembler;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.models.enums.StatutStage;
import net.ent.etnc.backend.services.StageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * TODO
 */
@RestController
@RequestMapping("/api/v1/stages/")
public class StageController {

    private static final Map<String, String> FILTERABLE = Map.of(
            "nom", "nom",
            "statut", "statut"
    );

    private static final Set<String> PATCHABLE = Set.of(
            "salleId"
    );

    private final StageService stageService;
    private final StageAssembler assembler;

    private final RsqlFilterUtils rsqlFilterUtils;
    private final JsonPatchUtils jsonPatchUtils;

    @Autowired
    public StageController(StageService stageService, StageAssembler assembler, RsqlFilterUtils rsqlFilterUtils, JsonPatchUtils jsonPatchUtils) {
        this.stageService = stageService;
        this.assembler = assembler;
        this.rsqlFilterUtils = rsqlFilterUtils;
        this.jsonPatchUtils = jsonPatchUtils;
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<Page<StageResponse>> getAll(
            @RequestParam(required = false) String filter,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Specification<Stage> spec = this.rsqlFilterUtils.build(filter, FILTERABLE);
        return ResponseEntity.ok(stageService.findAll(spec, pageable).map(assembler::toResponse));
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

    @PostMapping("import/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<StageImportResponse> postImport(@RequestBody StageImportRequest stageImportRequest) {
        List<Stage> stages = new ArrayList<>();
        for(StageRequest stageRequest : stageImportRequest.getStages()){
            Stage stage = assembler.toEntity(stageRequest);
            if(!stageService.existsByCode(stage.getCode())){
                stage.setStatut(StatutStage.A_AFFECTER);
                stageService.create(stage);
            }
        }
        return ResponseEntity.ok(assembler.toResponses(stages));
    }

    @PatchMapping("{id}/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<StageResponse> patch(@PathVariable Long id, @RequestBody String patch){
        Optional<Stage> existing = stageService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        StageRequest patched = jsonPatchUtils.apply(patch, assembler.toRequest(existing.get()), StageRequest.class, PATCHABLE);

        Stage entity = assembler.toEntity(patched);
        entity.setId(id);
        // on a attribuer une salle au stage, le stage passe en statut AFFECTER
        entity.setStatut(StatutStage.AFFECTER);
        entity = stageService.update(entity);
        return ResponseEntity.ok(assembler.toResponse(entity));
    }
}