package net.ent.etnc.backend.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.ent.etnc.backend.models.enums.TypeMateriel;

/**
 * Représente une unité central d'un poste.
 */
@Entity
@EqualsAndHashCode(callSuper = true, of = {})
@ToString(callSuper = true, of = {"processeur", "carteGraphique"})
public class UniteCentrale extends Materiel {
        //LBK
        @Getter
        @Setter
        //BV
        //JPA
        @Column(name = "processeur", nullable = true, length = 20)
        private String processeur;

        //LBK
        @Getter
        @Setter
        //BV
        //JPA
        @Column(name = "carte_graphique", nullable = true, length = 20)
        private String carteGraphique;

        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "type ne peut pas être null")
        //JPA
        @Enumerated(value = EnumType.STRING)
        @Column(name = "type", nullable = false, length = 20)
        private TypeMateriel type = TypeMateriel.UC;
}