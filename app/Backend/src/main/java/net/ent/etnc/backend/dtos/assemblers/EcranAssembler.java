package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.EcranDto;
import net.ent.etnc.backend.models.entities.Ecran;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class EcranAssembler {

    public EcranDto toDto(Ecran ecran) {
        return EcranDto.builder()
                .id(ecran.getId())
                .numSerie(ecran.getNumeroSerie())
                .marque(ecran.getMarque())
                .modele(ecran.getModele())
                .dateAjout(ecran.getDateAjout())
                .dateFinGarantie(ecran.getDateFinGarantie())
                .taille(ecran.getTaille())
                .build();
    }

    public List<EcranDto> toDtos(Collection<Ecran> ecrans) {
        return ecrans.stream().map(this::toDto).toList();
    }

    public Ecran toEntity(EcranDto ecranDto) {
        Ecran ecran = new Ecran();

        ecran.setNumeroSerie(ecranDto.getNumSerie());
        ecran.setDateAjout(ecranDto.getDateAjout());
        ecran.setDateFinGarantie(ecranDto.getDateFinGarantie());
        ecran.setMarque(ecranDto.getMarque());
        ecran.setModele(ecranDto.getModele());
        ecran.setTaille(ecranDto.getTaille());

        return ecran;
    }
}