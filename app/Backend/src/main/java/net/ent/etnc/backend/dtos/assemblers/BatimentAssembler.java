package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.responses.BatimentResponse;
import net.ent.etnc.backend.models.entities.Batiment;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class BatimentAssembler {

    public BatimentResponse toResponse(Batiment batiment) {
        return BatimentResponse.builder()
                .id(batiment.getId())
                .nom(batiment.getNom())
                .sallesId(
                        batiment.getSalles().stream()
                                .map(s -> s.getId())
                                .toList()
                        )
                .build();
    }

    public List<BatimentResponse> toResponses(Collection<Batiment> batiments) {
        return batiments.stream().map(this::toResponse).toList();
    }

    public Batiment toEntity(BatimentResponse batimentResponse) {
        Batiment batiment = new Batiment();

        batiment.setNom(batimentResponse.getNom());

        return batiment;
    }
}