package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;
import net.ent.etnc.backend.models.enums.StatutStage;

import java.time.LocalDate;

@Getter
@Setter
public class StageRequest {
    private String code;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    // id de la salle à laquelle le stage est affecté
    private Long salleId;
}
