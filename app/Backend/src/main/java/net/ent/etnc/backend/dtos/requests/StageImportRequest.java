package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Corps de requête pour l'importation de stages.
 */
@Getter
@Setter
public class StageImportRequest {
    private List<StageRequest> stages;
}
