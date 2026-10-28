package net.ent.etnc.backend.dtos.responses;

import lombok.*;

import java.util.List;

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
