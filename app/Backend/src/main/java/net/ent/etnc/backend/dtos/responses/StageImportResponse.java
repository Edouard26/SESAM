package net.ent.etnc.backend.dtos.responses;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageImportResponse {
    private List<StageResponse> stages;
}
