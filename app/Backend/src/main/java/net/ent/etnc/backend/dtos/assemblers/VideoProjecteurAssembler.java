package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.VideoProjecteurDto;
import net.ent.etnc.backend.models.entities.VideoProjecteur;
import org.springframework.stereotype.Component;

/**
 * Convertit entre {@link VideoProjecteur} et ses représentations DTO.
 */
@Component
public class VideoProjecteurAssembler {

    public VideoProjecteurDto toDto(VideoProjecteur videoProjecteur) {
        return VideoProjecteurDto.builder()
                .id(videoProjecteur.getId())
                .numSerie(videoProjecteur.getNumeroSerie())
                .marque(videoProjecteur.getMarque())
                .modele(videoProjecteur.getModele())
                .dateAjout(videoProjecteur.getDateAjout())
                .dateFinGarantie(videoProjecteur.getDateFinGarantie())
                .build();
    }

    public VideoProjecteur toEntity(VideoProjecteurDto videoProjecteurDto) {
        VideoProjecteur videoProjecteur = new VideoProjecteur();

        videoProjecteur.setNumeroSerie(videoProjecteurDto.getNumSerie());
        videoProjecteur.setDateAjout(videoProjecteurDto.getDateAjout());
        videoProjecteur.setDateFinGarantie(videoProjecteurDto.getDateFinGarantie());
        videoProjecteur.setMarque(videoProjecteurDto.getMarque());
        videoProjecteur.setModele(videoProjecteurDto.getModele());

        return videoProjecteur;
    }
}