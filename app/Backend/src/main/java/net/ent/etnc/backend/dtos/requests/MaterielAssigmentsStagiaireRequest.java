package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MaterielAssigmentsStagiaireRequest {
    private Long stagiaireId;
    private List<Long> materiels;
}
