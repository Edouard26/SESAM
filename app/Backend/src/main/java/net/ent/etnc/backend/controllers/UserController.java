package net.ent.etnc.backend.controllers;

import net.ent.etnc.backend.commons.RsqlFilterUtils;
import net.ent.etnc.backend.dtos.UserDto;
import net.ent.etnc.backend.dtos.assemblers.StageAssembler;
import net.ent.etnc.backend.dtos.assemblers.UserAssembler;
import net.ent.etnc.backend.dtos.requests.MaterielAssigmentsStagiaireRequest;
import net.ent.etnc.backend.dtos.requests.StagiaireImportRequest;
import net.ent.etnc.backend.dtos.requests.StagiaireRequest;
import net.ent.etnc.backend.dtos.responses.StageResponse;
import net.ent.etnc.backend.dtos.responses.StagiaireResponse;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.enums.Role;
import net.ent.etnc.backend.services.MaterielService;
import net.ent.etnc.backend.services.SalleService;
import net.ent.etnc.backend.services.StageService;
import net.ent.etnc.backend.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Endpoint REST pour la gestion des utilisateurs ({@code /api/v1/users/}).
 *
 * <p>Lecture réservée aux RESPONSABLE+. Création, modification et gestion du rôle
 * réservées aux ADMIN. Les transitions activation/désactivation passent par des
 * sous-ressources PATCH ({@code /{id}/activate/}, {@code /{id}/deactivate/}).
 */
@RestController
@RequestMapping("/api/v1/users/")
public class UserController {

    private static final Map<String, String> FILTERABLE = Map.of(
            "nid", "nid",
            "email", "email",
            "nom", "nom",
            "prenom", "prenom",
            "active", "active",
            "role", "role"
    );

    private final UserService userService;
    private final StageService stageService;
    private final MaterielService materielService;
    private final SalleService salleService;
    private final UserAssembler assembler;
    private final StageAssembler stageAssembler;

    private final RsqlFilterUtils rsqlFilterUtils;

    public UserController(UserService userService, StageService stageService, MaterielService materielService, SalleService salleService, UserAssembler assembler, StageAssembler stageAssembler, RsqlFilterUtils rsqlFilterUtils) {
        this.userService = userService;
        this.stageService = stageService;
        this.materielService = materielService;
        this.salleService = salleService;
        this.assembler = assembler;
        this.stageAssembler = stageAssembler;
        this.rsqlFilterUtils = rsqlFilterUtils;
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAll(
            @RequestParam(required = false) String filter,
            @PageableDefault(size = 20) Pageable pageable
    ){
        Specification<User> spec = this.rsqlFilterUtils.build(filter, FILTERABLE);
        return ResponseEntity.ok(userService.findAll(spec, pageable).map(assembler::toDto));
    }

    @GetMapping("{id}/")
    public ResponseEntity<UserDto> getById(@PathVariable Long id){
        return userService.findById(id)
                .map(a -> ResponseEntity.ok(assembler.toDto(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDto> post(@RequestBody UserDto userDto){
        return ResponseEntity.ok(assembler.toDto(userService.create(assembler.toEntity(userDto))));
    }

    @PostMapping("import/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    public ResponseEntity<StageResponse> postImportStagiaire(@RequestBody StagiaireImportRequest stagiaireImportRequest) {
        Optional<Stage> optionalStage = stageService.findById(stagiaireImportRequest.getStageId());
        if(optionalStage.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        List<User> stagiaires = new ArrayList<>();
        for(StagiaireRequest stagiaireRequest : stagiaireImportRequest.getStagiaires()){
            User stagiaire = assembler.toEntity(stagiaireRequest);

            stagiaire.setRole(Role.STAGIAIRE);
            stagiaire.setPassword("azerty+35");
            if(!userService.existsByNid(stagiaire.getNid())){
                userService.create(stagiaire);
            }// TODO dans le else seter L'id du stagiaire déjà présent dans la bdd
            optionalStage.get().getStagiaires().add(stagiaire);
            stagiaires.add(stagiaire);
        }

        return ResponseEntity.ok(stageAssembler.toResponse(stageService.update(optionalStage.get())));
    }

    @PostMapping("assignments/")
    @PreAuthorize("hasRole('GESTIONNAIRE')")
    // TODO remplacer le return StagiaireResponse
    public ResponseEntity<StagiaireResponse> postAssigmentsStagiaire(@RequestBody MaterielAssigmentsStagiaireRequest materielAssigmentsStagiaireRequest){
        Optional<User> optionalStagiaire = userService.findById(materielAssigmentsStagiaireRequest.getStagiaireId());
        if(optionalStagiaire.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        if(optionalStagiaire.get().getRole() != Role.STAGIAIRE){
            return ResponseEntity.badRequest().build();// TODO mettre dans le service RG
        }

        for(Long materielId : materielAssigmentsStagiaireRequest.getMaterielsId()){
            Optional<Materiel> optionalMateriel = materielService.findById(materielId);
            // RG: le matériel ne peut pas déjà être affecter un autre stagiaire
            // RG: le matériel doit être dans la salle du stage du stagiaire
            if(optionalMateriel.isPresent() && materielService.isAssignableToStagiaire(optionalMateriel.get(), salleService.findByStagiaire(optionalStagiaire.get()))){
//                optionalStagiaire.get().getMateriels().add(optionalMateriel.get());
                optionalMateriel.get().setStagiaire(optionalStagiaire.get());
            }
        }
        // TODO peut être renvoyer pour chaque matériel si il a bien été affecté
        return ResponseEntity.ok(assembler.toStagiaireResponse(userService.update(optionalStagiaire.get())));
    }

    @PutMapping("{id}/")
    public ResponseEntity<UserDto> put(@PathVariable Long id, @RequestBody UserDto userDto){
        if(!userService.existsById(id)) return ResponseEntity.notFound().build();
        userDto.setId(id);

        return ResponseEntity.ok(assembler.toDto(userService.update(assembler.toEntity(userDto))));
    }

    @DeleteMapping("{id}/")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if(!userService.existsById(id)) return ResponseEntity.notFound().build();

        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
