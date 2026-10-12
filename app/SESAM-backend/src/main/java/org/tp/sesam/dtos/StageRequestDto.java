package org.tp.sesam.dtos;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.tp.sesam.models.enums.TypeSalle;

@Getter
@Setter
public class StageRequestDto {
    @PositiveOrZero
    private Long version;
    @NotBlank @Size(max = 100)
    private String nom;
    @NotNull
    private LocalDate dateDebut;
    @NotNull
    private LocalDate dateFin;
}
