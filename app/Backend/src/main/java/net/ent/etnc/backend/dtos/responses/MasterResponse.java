package net.ent.etnc.backend.dtos.responses;

import lombok.*;

import java.util.List;

/**
 * Représentation sérialisable d'un master.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterResponse {
    private Long id;

    private String nom;
    private List<Long> processusId;
}
