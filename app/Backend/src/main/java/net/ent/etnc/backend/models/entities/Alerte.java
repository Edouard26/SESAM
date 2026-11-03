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
import net.ent.etnc.backend.models.enums.StatutAlerte;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant une alerte.
 * <p>Une alerte est signalé par un stagiaire ou un formateur si il y a un problème sur un matériel.</p>
 * <p>
 *     Règle de gestion:<br>
 *              - une alerte est signalée par un stagiaire sur un de ses matériels ou par un formateur sur un matériel qui dans la salle dans lequel il fait cours<br>
 *              - une alerte est résolue par un gestionnaire ou un administrateur
 * </p>
 */
@Entity
@Table(
        name = "alertes",
        uniqueConstraints = {@UniqueConstraint(
                name = "uk___alertes___signaleur_id__date_signalement",
                columnNames = {"signaleur_id", "date_signalement"}
        )}
)
@EqualsAndHashCode(callSuper = false, of = {"signaleur", "dateSignalement"})
@ToString(callSuper = true, of = {})
public class Alerte extends AbstractPersistableWithIdSetter<Long> {

        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "signaleur ne peut pas être null")
        @Valid
        //JPA
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "signaleur_id", foreignKey = @ForeignKey(name = "fk___alertes___signaleur_id"), nullable = false)
        private User signaleur;

        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "date signalement ne peut pas être null")
        //JPA
        @Column(name = "date_signalement", nullable = false)
        private LocalDateTime dateSignalement;

        //LBK
        @Getter
        @Setter
        //BV
        @Valid
        //JPA
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "resolveur_id", foreignKey = @ForeignKey(name = "fk___alertes___resolveur_id"), nullable = true)
        private User resolveur;

        //LBK
        @Getter
        @Setter
        //BV
        //JPA
        @Column(name = "date_resolution", nullable = true)
        private LocalDateTime dateResolution;

        //LBK
        @Getter
        @Setter
        //BV
        @NotBlank(message = "typeProbleme ne peut pas être vide")
        @Size(min = 2, max = 50, message = "typeProbleme doit faire 2 caractères minimum et 50 maximum")
        //JPA
        @Column(name = "type_probleme", nullable = false, length = 50)
        private String typeProbleme;
        
        //LBK
        @Getter
        @Setter
        //BV
        @NotBlank(message = "description ne peut pas être vide")
        @Size(max = 150, message = "description doit faire 150 maximum")
        //JPA
        @Column(name = "description", nullable = false, length = 150)
        private String description;

        /**
         * Statut de l'alerte en fonction si il a déjà été traité ou non.
         * <p>EN_ATTENTE si le problème n'est pas encore résolu.<br>
         * TRAITEE si le problème a été résolu.<br>
         * ANNULEE si le problème n'était pas un vrai problème.</p>
         */
        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "statut ne peut pas être null")
        //JPA
        @Enumerated(value = EnumType.STRING)
        @Column(name = "statut", nullable = false, length = 15)
        private StatutAlerte statut;

        //LBK
        @Getter
        @Setter
        //BV
        @Valid
        //JPA
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "alerte_materiels",
                    joinColumns = @JoinColumn(name = "alerte_id", foreignKey = @ForeignKey(name = "fk___alerte_materiels___alerte_id")),
                    inverseJoinColumns = @JoinColumn(name = "materiel_id", foreignKey = @ForeignKey(name = "fk___alerte_materiels___materiel_id"))
        )
        private List<Materiel> materiels = new ArrayList<>();
}
