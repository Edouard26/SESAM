package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Corps de requête pour l'importation de matériels et l'affectation dans une salle.
 */
@Getter
@Setter
public class MaterielImportRequest {
    private Long salleId;

    private List<MaterielRequest> materiels;
}
