package org.tp.sesam.dtos;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.models.enums.TypeSalle;
import org.springframework.stereotype.Component;

@Component
public class ProcessusDtoAssembler {
    public Processus toEntity(ProcessusRequestDto dto) {
        Processus entity = new Processus();
        entity.setVersion(dto.getVersion());
        entity.setNom(dto.getNom());
        return entity;
    }
    public ProcessusResponseDto toDto(Processus entity) {
        ProcessusResponseDto dto = new ProcessusResponseDto();
        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());
        dto.setNom(entity.getNom());
        return dto;
    }
}
