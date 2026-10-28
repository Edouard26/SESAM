package net.ent.etnc.backend.dtos.responses;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessusResponse {
    private Long id;
    private String nom;
}
