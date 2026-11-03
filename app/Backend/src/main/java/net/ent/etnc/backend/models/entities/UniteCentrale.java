package net.ent.etnc.backend.models.entities;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.ent.etnc.backend.models.enums.TypeMateriel;
import org.hibernate.annotations.MapKeyType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        @Column(name = "processeur", nullable = true, length = 30)
        private String processeur;

        //LBK
        @Getter
        @Setter
        //BV
        //JPA
        @Column(name = "carte_graphique", nullable = true, length = 30)
        private String carteGraphique;

        //LBK
        @Getter
        @Setter
        //BV
        @Valid
        //JPA
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "unite_central__processus",
                    joinColumns = @JoinColumn(name = "unite_central_id", foreignKey = @ForeignKey(name = "fk___unite_central__processus___unite_central_id")),
                    inverseJoinColumns = @JoinColumn(name = "processus_id", foreignKey = @ForeignKey(name = "fk___unite_central__processus___processus_id")))
        private List<Processus> processus = new ArrayList<>();

        // TODO ajouter adresse MAC

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