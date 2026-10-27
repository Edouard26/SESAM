package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.requests.MasterRequest;
import net.ent.etnc.backend.dtos.responses.MasterResponse;
import net.ent.etnc.backend.models.entities.Master;
import net.ent.etnc.backend.models.entities.Processus;
import net.ent.etnc.backend.services.ProcessusService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MasterAssembler {

    private final ProcessusService processusService;

    public MasterAssembler(ProcessusService processusService) {
        this.processusService = processusService;
    }

    public MasterResponse toResponse(Master master){
        return MasterResponse.builder()
                .id(master.getId())
                .nom(master.getNom())
                .processusId(
                        master.getProcessuss().stream()
                                .map(p -> p.getId())
                                .toList()
                )
                .build();
    }

    public Master toEntity(MasterRequest masterRequest){
        Master master = new Master();

        master.setNom(masterRequest.getNom());
        for(Long id : masterRequest.getProcessusId()){
            Optional<Processus> optionalProcessus = processusService.findById(id);
            optionalProcessus.ifPresent(processus -> master.getProcessuss().add(processus));
        }

        return master;
    }
}
