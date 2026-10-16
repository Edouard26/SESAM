package net.ent.etnc.backend.dtos.responses;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatimentResponse {
    private long id;

    private String nom;

    // Liste des id des salles situées dans le bâtiment
    private List<Long> sallesId;
}