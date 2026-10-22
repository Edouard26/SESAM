package net.ent.etnc.backend.dtos.requests;

import lombok.*;
import net.ent.etnc.backend.models.enums.StatutAlerte;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteResolutionRequest {
    private Long id;

    private Long resolveurId;
    private LocalDateTime dateResolution;
    private StatutAlerte statut;
}
