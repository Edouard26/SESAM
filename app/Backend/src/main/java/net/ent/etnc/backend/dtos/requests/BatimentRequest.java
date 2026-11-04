package net.ent.etnc.backend.dtos.requests;

import lombok.*;

/**
 * Corps de requête pour la crétaion ou la mise à jour d'un batiment.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatimentRequest {
    String nom;
}
