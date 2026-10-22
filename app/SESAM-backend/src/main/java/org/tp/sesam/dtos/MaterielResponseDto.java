package org.tp.sesam.dtos;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.tp.sesam.models.enums.TypeSalle;

@Getter
@Setter
public class MaterielResponseDto {
    private Long id;
    private Long version;
    private String numeroSerie;
    private LocalDate dateAjout;
    private LocalDate dateFinGarantie;
    private String marque;
}
