package net.ent.etnc.backend.dtos.responses;

import lombok.*;
import net.ent.etnc.backend.models.enums.StatutAlerte;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteResponse {
    private long id;

    private Long signaleurId;
    private LocalDateTime dateSignalement;
    private Long resolveurId;
    private LocalDateTime dateResolution;
    private String typeProbleme;
    private String description;
    private StatutAlerte statut;

    private List<Long> materielsId;
}