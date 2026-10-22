package net.ent.etnc.backend.dtos.requests;

import lombok.*;
import net.ent.etnc.backend.models.enums.EtatSalle;
import net.ent.etnc.backend.models.enums.TypeSalle;

@Getter
@Setter
public class SalleRequest {
    private String nom;
    private Integer nbPlaces;
    private TypeSalle typeSalle;
    private EtatSalle etat;

    private String nomBatiment;
}
