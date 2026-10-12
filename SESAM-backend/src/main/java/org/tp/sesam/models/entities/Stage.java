package org.tp.sesam.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.tp.sesam.models.commons.AbstractPersistableWithIdSetter;

import java.time.LocalDate;

// Route REST associée : /api/v1/stages
@Entity
@Table(name = "stages")
@Getter
@Setter
public class Stage extends AbstractPersistableWithIdSetter<Long> {

    /**
     * Nom visible du stage.
     * Exemples : "Formation Java", "Initiation réseau".
     */
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nom;

    /**
     * Date à laquelle le stage commence.
     */
    @NotNull
    @FutureOrPresent
    @Column(nullable = false)
    private LocalDate dateDebut;

    /**
     * Date à laquelle le stage se termine.
     * La règle "dateFin après dateDebut" sera contrôlée séparément.
     */
    @NotNull
    @FutureOrPresent
    @Column(nullable = false)
    private LocalDate dateFin;
}