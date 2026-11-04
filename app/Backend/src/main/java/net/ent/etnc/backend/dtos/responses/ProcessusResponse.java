package net.ent.etnc.backend.dtos.responses;

import lombok.*;

/**
 * Représentation sérialisable d'un processus.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessusResponse {
    private Long id;
    private String nom;
}
