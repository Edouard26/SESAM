package net.ent.etnc.backend.dtos.assemblers;

import net.ent.etnc.backend.dtos.VideoProjecteurDto;
import net.ent.etnc.backend.models.entities.VideoProjecteur;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

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

    public List<VideoProjecteurDto> toDtos(Collection<VideoProjecteur> videoProjecteurs) {
        return videoProjecteurs.stream().map(this::toDto).toList();
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