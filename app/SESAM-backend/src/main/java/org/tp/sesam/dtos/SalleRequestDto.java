package org.tp.sesam.dtos;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.tp.sesam.models.enums.TypeSalle;

@Getter
@Setter
public class SalleRequestDto {
    @PositiveOrZero
    private Long version;
    @NotBlank @Size(max = 100)
    private String nom;
    @NotNull
    private TypeSalle type;
}
