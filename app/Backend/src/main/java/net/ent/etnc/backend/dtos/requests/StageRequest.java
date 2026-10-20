package net.ent.etnc.backend.dtos.requests;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.ent.etnc.backend.models.validators.groups.UpdateGroup;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageRequest {
    private String code;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    // id de la salle à laquelle le stage est affecté
    @NotNull(groups = UpdateGroup.class)
    private Long salleId;
}
