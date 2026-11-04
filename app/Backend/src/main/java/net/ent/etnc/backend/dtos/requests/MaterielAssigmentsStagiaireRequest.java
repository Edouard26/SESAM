package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Corps de requête pour l'affectation de matériels à un stagiaire.
 */
@Getter
@Setter
public class MaterielAssigmentsStagiaireRequest {
    private Long stagiaireId;
    private List<Long> materielsId;
}
