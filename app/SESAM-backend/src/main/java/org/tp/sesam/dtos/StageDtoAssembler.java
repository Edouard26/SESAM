package org.tp.sesam.dtos;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.models.enums.TypeSalle;
import org.springframework.stereotype.Component;

@Component
public class StageDtoAssembler {
    public Stage toEntity(StageRequestDto dto) {
        Stage entity = new Stage();
        entity.setVersion(dto.getVersion());
        entity.setNom(dto.getNom());
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        return entity;
    }
    public StageResponseDto toDto(Stage entity) {
        StageResponseDto dto = new StageResponseDto();
        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());
        dto.setNom(entity.getNom());
        dto.setDateDebut(entity.getDateDebut());
        dto.setDateFin(entity.getDateFin());
        return dto;
    }
}
