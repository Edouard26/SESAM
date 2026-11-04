package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Corps de requête pour le signalement d'une alerte.
 */
@Getter
@Setter
public class AlerteSignalementRequest {

    private Long signaleurId;
    private LocalDateTime dateSignalement;
    private String typeProbleme;
    private String description;

    private List<Long> materielsId;
}
