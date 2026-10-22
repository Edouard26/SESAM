package net.ent.etnc.backend.dtos.requests;

import lombok.*;
import net.ent.etnc.backend.models.enums.EtatMateriel;
import net.ent.etnc.backend.models.enums.TypeMateriel;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterielRequest {

//    private String typeMateriel;
    private TypeMateriel typeMateriel;

    private String numeroSerie;
    private String nom;
    private String marque;
    private String modele;
    private LocalDate dateAjout;
    private LocalDate dateFinGarantie;
    private EtatMateriel etat;

    // si c'est un UC
    private String processeur;
    private String carteGraphique;

    // si c'est un écran
    private Integer taille;

    // si c'est un vidéo projecteur
}
