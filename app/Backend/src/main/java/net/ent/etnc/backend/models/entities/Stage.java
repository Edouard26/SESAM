package net.ent.etnc.backend.models.entities;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.ent.etnc.backend.models.commons.AbstractPersistableWithIdSetter;
import net.ent.etnc.backend.models.enums.StatutStage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Représente un stage.
 *
 * Règle de gestion:
 *      - le stage est placé dans une salle de cours.
 *      - un stage a le statut:
 *              . {@code A_AFFECTER} si le stage n'est pas affecté à une salle.
 *              . {@code AFFECTER} si le stage est affectée à une salle et le stage n'est pas encore commencé.
 *              . {@code EN_COURS} si le stage est commencé et n'est pas terminé.
 *              . {@code CLOTURER} si le stage est terminé.
 */
@Entity
@Table(
        name = "stages",
        uniqueConstraints = {@UniqueConstraint(
                name = "uk___stages___code",
                columnNames = {"code"}
        )}
)
@EqualsAndHashCode(callSuper = false, of = {"code"})
@ToString(callSuper = true, of = {"code","dateDebut","dateFin"})
public class Stage extends AbstractPersistableWithIdSetter<Long> {

        //LBK
        @Getter
        @Setter
        //BV
        @NotBlank(message = "code ne peut pas être vide")
        @Pattern(regexp = "^[A-Z]{2}[0-9]{2}$")
        //JPA
        @Column(name = "code", nullable = false, length = 4)
        private String code;

        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "date de debut ne peut pas être null")
        //JPA
        @Column(name = "date_debut", nullable = false)
        private LocalDate dateDebut;

        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "date de fin ne peut pas être null")
        //JPA
        @Column(name = "date_fin", nullable = false)
        private LocalDate dateFin;
        
        //LBK
        @Getter
        @Setter
        //BV
        @Valid
        //JPA
        @OneToMany(fetch = FetchType.LAZY)
        @JoinColumn(name = "stage_id", foreignKey = @ForeignKey(name = "fk___stagiaire___stage_id"))
        private Set<User> stagiaires = new HashSet<>();

        //LBK
        @Getter
        @Setter
        //BV
        @Valid
        //JPA
        @OneToOne(fetch = FetchType.LAZY) // TODO à réfléchir si @ManyToOne ou @OneToOne
        @JoinColumn(name = "salle_cours_id", foreignKey = @ForeignKey(name = "fk___stage___salle_cours_id"), nullable = true)
        private Salle salleCours;

        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "statut ne peut pas être null")
        //JPA
        @Enumerated(value = EnumType.STRING)
        @Column(name = "statut", nullable = false, length = 15)
        private StatutStage statut;
}