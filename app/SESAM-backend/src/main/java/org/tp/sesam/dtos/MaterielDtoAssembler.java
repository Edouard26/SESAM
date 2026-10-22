package org.tp.sesam.dtos;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.models.enums.TypeSalle;
import org.springframework.stereotype.Component;

@Component
public class MaterielDtoAssembler {
    public Materiel toEntity(MaterielRequestDto dto) {
        Materiel entity = new Materiel();
        entity.setVersion(dto.getVersion());
        entity.setNumeroSerie(dto.getNumeroSerie());
        entity.setDateAjout(dto.getDateAjout());
        entity.setDateFinGarantie(dto.getDateFinGarantie());
        entity.setMarque(dto.getMarque());
        return entity;
    }
    public MaterielResponseDto toDto(Materiel entity) {
        MaterielResponseDto dto = new MaterielResponseDto();
        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());
        dto.setNumeroSerie(entity.getNumeroSerie());
        dto.setDateAjout(entity.getDateAjout());
        dto.setDateFinGarantie(entity.getDateFinGarantie());
        dto.setMarque(entity.getMarque());
        return dto;
    }
}
