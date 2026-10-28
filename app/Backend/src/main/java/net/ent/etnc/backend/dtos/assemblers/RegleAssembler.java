package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.requests.RegleRequest;
import net.ent.etnc.backend.dtos.responses.RegleResponse;
import net.ent.etnc.backend.models.entities.Processus;
import net.ent.etnc.backend.models.entities.Regle;
import net.ent.etnc.backend.services.ProcessusService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RegleAssembler {

    private final ProcessusService processusService;

    public RegleAssembler(ProcessusService processusService) {
        this.processusService = processusService;
    }

    public RegleResponse toResponse(Regle regle){
        return RegleResponse.builder()
                .id(regle.getId())
                .nom(regle.getNom())
                .processusId(
                        regle.getProcessuss().stream()
                                .map(p -> p.getId())
                                .toList()
                )
                .build();
    }

    public Regle toEntity(RegleRequest regleRequest){
        Regle regle = new Regle();

        regle.setNom(regleRequest.getNom());
        for(Long id : regleRequest.getProcessusId()){
            Optional<Processus> optionalProcessus = processusService.findById(id);
            optionalProcessus.ifPresent(processus -> regle.getProcessuss().add(processus));
        }

        return regle;
    }
}
