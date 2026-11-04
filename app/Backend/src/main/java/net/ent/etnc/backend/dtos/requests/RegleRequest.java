package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Corps de requête pour la création ou la mise à jour d'une règle.
 */
@Getter
@Setter
public class RegleRequest {
    private String nom;
    private List<Long> processusId;
}
