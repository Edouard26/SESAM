package net.ent.etnc.backend.dtos.responses;

import lombok.*;
import net.ent.etnc.backend.dtos.requests.MaterielRequest;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterielImportResponse {
    private Long salleId;

    private List<MaterielResponse> materiels;
}
