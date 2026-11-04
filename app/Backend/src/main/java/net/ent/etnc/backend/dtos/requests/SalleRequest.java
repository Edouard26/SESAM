package net.ent.etnc.backend.dtos.requests;

import lombok.*;
import net.ent.etnc.backend.models.enums.EtatSalle;
import net.ent.etnc.backend.models.enums.TypeSalle;

/**
 * Corps de requête pour la création ou la mise à jour d'un salle.
 */
@Getter
@Setter
public class SalleRequest {
    private String nom;
    private Integer nbPlaces;
    private TypeSalle typeSalle;
    private EtatSalle etat;

    private String nomBatiment;
}
