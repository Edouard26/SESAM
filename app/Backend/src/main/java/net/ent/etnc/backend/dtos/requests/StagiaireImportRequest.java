package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StagiaireImportRequest {
    private Long stageId;
    private List<StagiaireRequest> stagiaires;
}
