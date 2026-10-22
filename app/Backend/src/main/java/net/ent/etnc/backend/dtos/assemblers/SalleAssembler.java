package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.requests.SalleRequest;
import net.ent.etnc.backend.dtos.responses.SalleResponse;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.enums.EtatMateriel;
import net.ent.etnc.backend.services.BatimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class SalleAssembler {

    private final BatimentService batimentService;
    private final MaterielAssembler materielAssembler;

    @Autowired
    public SalleAssembler(BatimentService batimentService, MaterielAssembler materielAssembler) {
        this.batimentService = batimentService;
        this.materielAssembler = materielAssembler;
    }

    public SalleResponse toResponse(Salle salle) {
        return SalleResponse.builder()
                .id(salle.getId())
                .nom(salle.getNom())
                .typeSalle(salle.getTypeSalle())
                .nbPlaces(salle.getNbPlaces())
                .etat(salle.getEtat())
                .nomBatiment(batimentService.findNomBySalleId(salle.getId()))
                .nbMaterielDispo(
                        salle.getMateriels().stream()
                                .filter(m -> m.getEtat() == EtatMateriel.EN_ETAT)
                                .count()
                )
                .materiels(
                        materielAssembler.toResponses(salle.getMateriels())
                )
                .build();
    }

    public List<SalleResponse> toResponses(Collection<Salle> salles) {
        return salles.stream().map(this::toResponse).toList();
    }

    public Salle toEntity(SalleRequest salleRequest) {
        Salle salle = new Salle();

        salle.setNom(salleRequest.getNom());
        salle.setTypeSalle(salleRequest.getTypeSalle());
        salle.setNbPlaces(salleRequest.getNbPlaces());
        salle.setEtat(salleRequest.getEtat());

        return salle;
    }
}