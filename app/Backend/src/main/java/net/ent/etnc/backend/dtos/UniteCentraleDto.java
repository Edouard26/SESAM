package net.ent.etnc.backend.dtos;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniteCentraleDto {
    private long id;

    private String numSerie;
    private String nom;
    private String marque;
    private String modele;
    private LocalDate dateAjout;
    private LocalDate dateFinGarantie;
    private String CPU;
    private String GPU;
}