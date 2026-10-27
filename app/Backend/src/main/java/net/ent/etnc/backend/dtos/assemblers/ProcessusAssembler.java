package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.requests.ProcessusRequest;
import net.ent.etnc.backend.dtos.responses.ProcessusResponse;
import net.ent.etnc.backend.models.entities.Processus;
import org.springframework.stereotype.Component;

@Component
public class ProcessusAssembler {

    public ProcessusResponse toResponse(Processus processus){
        return ProcessusResponse.builder()
                .id(processus.getId())
                .nom(processus.getNom())
                .build();
    }

    public Processus toEntity(ProcessusRequest processusRequest){
        Processus processus = new Processus();

        processus.setNom(processusRequest.getNom());

        return processus;
    }
}
