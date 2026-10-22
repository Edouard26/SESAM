package org.tp.sesam.dtos;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.tp.sesam.models.enums.TypeSalle;

@Getter
@Setter
public class SalleResponseDto {
    private Long id;
    private Long version;
    private String nom;
    private TypeSalle type;
}
