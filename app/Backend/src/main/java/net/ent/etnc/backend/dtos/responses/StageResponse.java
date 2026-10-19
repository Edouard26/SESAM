package net.ent.etnc.backend.dtos.responses;

import lombok.*;
import net.ent.etnc.backend.models.enums.StatutStage;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageResponse {
    private long id;

    private String code;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutStage statut;

    // Liste des ids des stagiaires affectés au stage
    private List<Long> stagiairesId;

    // id de la salle à laquelle le stage est affecté
    private Long salleId;
}