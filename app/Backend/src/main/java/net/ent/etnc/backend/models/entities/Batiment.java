package net.ent.etnc.backend.models.entities;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.ent.etnc.backend.models.commons.AbstractPersistableWithIdSetter;

import java.util.HashSet;
import java.util.Set;

/**
 * Représente un bâtiment.
 *
 * Il est constitué d'une liste de salle.
 */
@Entity
@Table(
        name = "batiments",
        uniqueConstraints = {@UniqueConstraint(
                name = "uk___batiments___nom",
                columnNames = {"nom"}
        )}
)
@EqualsAndHashCode(callSuper = false, of = {"nom"})
@ToString(callSuper = true, of = {"nom"})
public class Batiment extends AbstractPersistableWithIdSetter<Long> {

        //LBK
        @Getter
        @Setter
        //BV
        @NotBlank(message = "nom du batiment ne peut pas être vide")
        @Size(min = 2, max = 10, message = "nom du batiment doit faire 2 caractères minimum et 10 maximum")
        //JPA
        @Column(name = "nom", nullable = false, length = 10)
        private String nom;
        
        //LBK
        @Getter
        @Setter
        //BV
        @Valid
        //JPA
        @OneToMany(fetch = FetchType.LAZY)
        @JoinColumn(name = "batiment_id", foreignKey = @ForeignKey(name = "fk___salles___batiment_id"))
        private Set<Salle> salles = new HashSet<>();
}