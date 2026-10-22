package org.tp.sesam.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.tp.sesam.models.commons.AbstractPersistableWithIdSetter;

/**
 * Représente un processus de l'application.
 * Exemple : "Gestion des stages".
 */
@Entity
@Table(
        name = "processus",
        uniqueConstraints = @UniqueConstraint(
                name = "uk__processus__nom",
                columnNames = "nom"
        )
)
@Getter
@Setter
public class Processus extends AbstractPersistableWithIdSetter<Long> {

    /**
     * Nom unique du processus.
     */
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nom;
}