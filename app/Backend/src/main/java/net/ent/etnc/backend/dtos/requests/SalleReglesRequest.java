package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Corps de requête pour l'application d'une règle à une salle.
 */
@Getter
@Setter
public class SalleReglesRequest {
    private List<Long> reglesId;
}
