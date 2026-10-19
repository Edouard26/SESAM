package net.ent.etnc.backend.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.ent.etnc.backend.models.enums.TypeMateriel;

/**
 * TODO
 */
@Entity
//@Table(
//        name = "ecrans",
//        uniqueConstraints = {@UniqueConstraint(
//                name = "uk___ecrans___numero_serie",
//                columnNames = {"numero_serie"}
//        )}
//)
@EqualsAndHashCode(callSuper = true, of = {})
@ToString(callSuper = true, of = {"taille"})
public class Ecran extends Materiel {

        /** Représente la taille de l'écran en pouces. */
        //LBK
        @Getter
        @Setter
        //BV
//        @NotNull(message = "taille ne peut pas être null")
        //JPA
        @Column(name = "taille", nullable = true)
        private Integer taille;

        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "type ne peut pas être null")
        //JPA
        @Enumerated(value = EnumType.STRING)
        @Column(name = "type", nullable = false, length = 20)
        private TypeMateriel type = TypeMateriel.ECRAN;
}