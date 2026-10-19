package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.UniteCentraleDto;
import net.ent.etnc.backend.models.entities.UniteCentrale;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class UniteCentraleAssembler {

    public UniteCentraleDto toDto(UniteCentrale uniteCentrale) {
        return UniteCentraleDto.builder()
                .id(uniteCentrale.getId())
                .numSerie(uniteCentrale.getNumeroSerie())
                .nom(uniteCentrale.getNom())
                .marque(uniteCentrale.getMarque())
                .modele(uniteCentrale.getModele())
                .dateAjout(uniteCentrale.getDateAjout())
                .dateFinGarantie(uniteCentrale.getDateFinGarantie())
                .CPU(uniteCentrale.getProcesseur())
                .GPU(uniteCentrale.getCarteGraphique())
                .build();
    }

    public List<UniteCentraleDto> toDtos(Collection<UniteCentrale> uniteCentrales) {
        return uniteCentrales.stream().map(this::toDto).toList();
    }

    public UniteCentrale toEntity(UniteCentraleDto uniteCentraleDto) {
        UniteCentrale uc = new UniteCentrale();

        uc.setNumeroSerie(uniteCentraleDto.getNumSerie());
        uc.setNom(uniteCentraleDto.getNom());
        uc.setDateAjout(uniteCentraleDto.getDateAjout());
        uc.setDateFinGarantie(uniteCentraleDto.getDateFinGarantie());
        uc.setMarque(uniteCentraleDto.getMarque());
        uc.setModele(uniteCentraleDto.getModele());
        uc.setProcesseur(uniteCentraleDto.getCPU());
        uc.setCarteGraphique(uniteCentraleDto.getGPU());

        return uc;
    }
}