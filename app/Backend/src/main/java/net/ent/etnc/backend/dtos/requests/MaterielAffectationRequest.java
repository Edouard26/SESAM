package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request pour l'affectation de matériels dans une nouvelle salle.
 */
@Getter
@Setter
public class MaterielAffectationRequest {
    private Long salleId;
    private List<Long> materielsId;
}
