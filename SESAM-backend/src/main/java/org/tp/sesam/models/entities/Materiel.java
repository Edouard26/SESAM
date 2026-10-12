package org.tp.sesam.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.tp.sesam.models.commons.AbstractPersistableWithIdSetter;
import org.tp.sesam.models.validators.dateRange.ValidDateRange;

import java.time.LocalDate;


/**
 * Représente un matériel suivi dans l'établissement.
 */

@Entity
@Table(name = "materiels")
@Getter
@Setter
public class Materiel extends AbstractPersistableWithIdSetter<Long> {

    /**
     * Identifiant physique unique indiqué sur le matériel.
     */
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String numeroSerie;

    /**
     * Date d'entrée du matériel dans l'inventaire.
     */
    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private LocalDate dateAjout;

    /**
     * Dernier jour de validité de la garantie.
     */
    @NotNull
    @FutureOrPresent
    @Column(nullable = false)
    private LocalDate dateFinGarantie;

    /**
     * Fabricant du matériel.
     * Exemple : Dell, Cisco ou Lenovo.
     */
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String marque;
}