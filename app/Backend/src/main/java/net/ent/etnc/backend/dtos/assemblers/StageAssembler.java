package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.requests.StageRequest;
import net.ent.etnc.backend.dtos.responses.StageResponse;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.services.SalleService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class StageAssembler {

    private final SalleService salleService;

    public StageAssembler(SalleService salleService) {
        this.salleService = salleService;
    }

    public StageResponse toResponse(Stage stage) {
        return StageResponse.builder()
                .id(stage.getId())
                .code(stage.getCode())
                .dateDebut(stage.getDateDebut())
                .dateFin(stage.getDateFin())
                .statut(stage.getStatut())
                .stagiairesId(
                        stage.getStagiaires().stream()
                                .map(s -> s.getId())
                                .toList()
                )
                .salleId(stage.getSalleCours() == null ? null : stage.getSalleCours().getId())
                .build();
    }

    public List<StageResponse> toDtos(Collection<Stage> stages) {
        return stages.stream().map(this::toResponse).toList();
    }

    public Stage toEntity(StageRequest stageRequest) {
        Stage stage = new Stage();

        stage.setCode(stageRequest.getCode());
        stage.setDateDebut(stageRequest.getDateDebut());
        stage.setDateFin(stageRequest.getDateFin());
        if(stageRequest.getSalleId() != null){ // on vérifie si on nous renvoie l'id de la salle
            Optional<Salle> optionalSalle = salleService.findById(stageRequest.getSalleId());
            if(optionalSalle.isEmpty()){
                throw new ServiceException("Salle not found");
            }
            stage.setSalleCours(optionalSalle.get());
        }

        return stage;
    }
}