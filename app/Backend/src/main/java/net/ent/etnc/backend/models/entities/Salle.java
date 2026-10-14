package net.ent.etnc.backend.models.entities;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.ent.etnc.backend.models.commons.AbstractPersistableWithIdSetter;
import net.ent.etnc.backend.models.enums.EtatSalle;
import net.ent.etnc.backend.models.enums.TypeSalle;

import java.util.HashSet;
import java.util.Set;

/**
 * Représente une salle dans un bâtiment.
 *
 * Une salle peut être une salle de stockage ou de cours. Seul les salles de cours peuvent accueilir des stagiaires.
 * Elle possède une liste de matériel et a un nombre limité de postes.
 *
 */
@Entity
@Table(
        name = "salles",
        uniqueConstraints = {@UniqueConstraint(
                name = "uk___salles___nom",
                columnNames = {"nom"}
        )}
)
@EqualsAndHashCode(callSuper = false, of = {"nom"})
@ToString(callSuper = true, of = {"nom", "nbPlaces", "typeSalle"})
public class Salle extends AbstractPersistableWithIdSetter<Long> {

        //LBK
        @Getter
        @Setter
        //BV
        @NotBlank(message = "nom de la salle ne peut pas être vide")
        @Size(min = 2, max = 10, message = "nom de la salle doit faire 2 caractères minimum et 10 maximum")
        //JPA
        @Column(name = "nom", nullable = false, length = 10)
        private String nom;

        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "nbPlaces ne peut pas être null")
        //JPA
        @Column(name = "nbPlaces", nullable = false)
        private Integer nbPlaces;

        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "le type de la salle ne peut pas être null")
        //JPA
        @Enumerated(value = EnumType.STRING)
        @Column(name = "type", nullable = false, length = 10)
        private TypeSalle typeSalle;

        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "l'état ne peut pas être null")
        //JPA
        @Enumerated(value = EnumType.STRING)
        @Column(name = "etat", nullable = false, length = 15)
        private EtatSalle etat;

        //LBK
        @Getter
        @Setter
        //BV
        @Valid
        //JPA
        @OneToMany(fetch = FetchType.LAZY)
        @JoinColumn(name = "salle_id", foreignKey = @ForeignKey(name = "fk___materiels___salle_id"))
        private Set<Materiel> materiels = new HashSet<>();
}