package org.tp.sesam.dtos;

import org.springframework.stereotype.Component;
import org.tp.sesam.models.entities.Salle;

@Component
public class SalleDtoAssembler {

    public Salle toEntity(SalleRequestDto dto) {
        Salle entity = new Salle();
        entity.setVersion(dto.getVersion());
        entity.setNom(dto.getNom());
        entity.setTypeSalle(dto.getType());
        return entity;
    }

    public SalleResponseDto toDto(Salle entity) {
        SalleResponseDto dto = new SalleResponseDto();
        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());
        dto.setNom(entity.getNom());
        dto.setType(entity.getTypeSalle());
        return dto;
    }
}