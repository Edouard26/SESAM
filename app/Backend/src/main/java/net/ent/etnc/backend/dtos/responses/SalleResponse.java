package net.ent.etnc.backend.dtos.responses;

import lombok.*;
import net.ent.etnc.backend.models.enums.EtatSalle;
import net.ent.etnc.backend.models.enums.TypeSalle;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalleResponse {
    private long id;

    private String nom;
    private Integer nbPlaces;
    private TypeSalle typeSalle;
    private EtatSalle etat;

    // nom du batiment dans lequel la salle se situe
    private String nomBatiment;

    // nombre de matériel en état de fonctionner dans la salle
    private Long nbMaterielDispo;


    // Liste des id des matériels dans la salle
    private List<Long> materielsId;
}