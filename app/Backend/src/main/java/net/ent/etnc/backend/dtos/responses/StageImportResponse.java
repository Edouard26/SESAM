package net.ent.etnc.backend.dtos.responses;

import lombok.*;

import java.util.List;

/**
 * Réponse renvoyé après une importation de stages.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageImportResponse {
    private List<StageResponse> stages;
}
