package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.requests.ProcessusRequest;
import net.ent.etnc.backend.dtos.responses.ProcessusResponse;
import net.ent.etnc.backend.models.entities.Processus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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
