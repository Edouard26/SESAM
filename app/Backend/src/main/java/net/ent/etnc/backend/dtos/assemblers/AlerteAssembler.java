package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.requests.AlerteResolutionRequest;
import net.ent.etnc.backend.dtos.requests.AlerteSignalementRequest;
import net.ent.etnc.backend.dtos.responses.AlerteResponse;
import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.enums.StatutAlerte;
import net.ent.etnc.backend.services.MaterielService;
import net.ent.etnc.backend.services.UserService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class AlerteAssembler {

    private final UserService userService;
    private final MaterielService materielService;

    public AlerteAssembler(UserService userService, MaterielService materielService) {
        this.userService = userService;
        this.materielService = materielService;
    }

    public AlerteResponse toResponse(Alerte alerte) {
        return AlerteResponse.builder()
                .id(alerte.getId())
                .signaleurId(alerte.getSignaleur().getId())
                .dateSignalement(alerte.getDateSignalement())
                .resolveurId(alerte.getResolveur() == null ? null : alerte.getResolveur().getId())
                .dateResolution(alerte.getDateResolution() == null ? null : alerte.getDateResolution())
                .typeProbleme(alerte.getTypeProbleme())
                .description(alerte.getDescription())
                .statut(alerte.getStatut())
                .materielsId(
                        alerte.getMateriels().stream()
                                .map(m -> m.getId())
                                .toList()
                )
                .build();
    }

    public List<AlerteResponse> toDtos(Collection<Alerte> alertes) {
        return alertes.stream().map(this::toResponse).toList();
    }

    public AlerteResolutionRequest toRequest(Alerte alerte){
        return AlerteResolutionRequest.builder()// TODO à améliorer
                .resolveurId(alerte.getResolveur() == null ? null : alerte.getResolveur().getId())
                .dateResolution(alerte.getDateResolution() == null ? null : alerte.getDateResolution())
                .statut(alerte.getStatut())
                .build();
    }

    public Alerte toEntityPatch(Alerte alerte, AlerteResolutionRequest patch){
        Alerte a = new Alerte();

        a.setSignaleur(alerte.getSignaleur());
        a.setDateSignalement(alerte.getDateSignalement());
        a.setTypeProbleme(alerte.getTypeProbleme());
        a.setDescription(alerte.getDescription());
        a.setMateriels(alerte.getMateriels());

        Optional<User> optional = userService.findById(patch.getResolveurId());
        if(optional.isEmpty()){
            throw new ServiceException("User not found");
        }
        a.setResolveur(optional.get());
        a.setDateResolution(patch.getDateResolution());
        a.setStatut(patch.getStatut());

        return a;
    }

    public Alerte toEntity(AlerteSignalementRequest alerteSignalementRequest) {
        Alerte alerte = new Alerte();

        Optional<User> optionalSignaleur = userService.findById(alerteSignalementRequest.getSignaleurId());
        // TODO vérifier dans le service
        if(optionalSignaleur.isEmpty()){
            throw new ServiceException("User not found");
        }
        alerte.setSignaleur(optionalSignaleur.get());
        alerte.setDateSignalement(alerteSignalementRequest.getDateSignalement());
        alerte.setTypeProbleme(alerteSignalementRequest.getTypeProbleme());
        alerte.setDescription(alerteSignalementRequest.getDescription());

        for(Long materielId : alerteSignalementRequest.getMaterielsId()){
            Optional<Materiel> optionalMateriel = materielService.findById(materielId);
            optionalMateriel.ifPresent(materiel -> alerte.getMateriels().add(materiel));
        }

        alerte.setStatut(StatutAlerte.EN_ATTENTE);

        return alerte;
    }
}