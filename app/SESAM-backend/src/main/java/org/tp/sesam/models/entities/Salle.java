package org.tp.sesam.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.tp.sesam.models.commons.AbstractPersistableWithIdSetter;
import org.tp.sesam.models.enums.TypeSalle;

/**
 * Salle de cours ou de stockage, identifiée par son type.
 */
@Entity
@Table(name = "salles")
@Getter
@Setter
public class Salle extends AbstractPersistableWithIdSetter<Long> {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nom;

    // Stocke "SALLE_COURS" ou "SALLE_STOCKAGE" en BDD.
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeSalle typeSalle;
}