package net.ent.etnc.backend.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.ent.etnc.backend.models.commons.AbstractPersistableWithIdSetter;
import net.ent.etnc.backend.models.enums.EtatMateriel;

import java.time.LocalDate;

/**
 * Superclass Materiel représentant les matériels du parc informatique.
 */
//@MappedSuperclass
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(
        name = "materiel",
        uniqueConstraints = {@UniqueConstraint(
                name = "uk___materiel___numero_serie",
                columnNames = {"numero_serie"}
        )}
)
@EqualsAndHashCode(callSuper = false, of = {"numeroSerie"})
@ToString(callSuper = true, of = {"numeroSerie", "marque", "modele", "dataAjout", "dateFinGarantie"})
public abstract class Materiel extends AbstractPersistableWithIdSetter<Long> {

        //LBK
        @Getter
        @Setter
        //BV
        @NotBlank(message = "Numéro de série ne peut pas être vide")
        @Size(min = 2, max = 20, message = "Numéro de série doit faire 2 caractères minimum et 20 maximum")
        //JPA
        @Column(name = "numero_serie", nullable = false, length = 20)
        private String numeroSerie;

        //LBK
        @Getter
        @Setter
        //BV
        @NotBlank(message = "nom ne peut pas être vide")
        @Size(min = 2, max = 20, message = "nom doit faire 2 caractères minimum et 20 maximum")
        //JPA
        @Column(name = "nom", nullable = false, length = 20)
        private String nom;

        //LBK
        @Getter
        @Setter
        //BV
        @NotBlank(message = "marque ne peut pas être vide")
        @Size(min = 2, max = 20, message = "marque doit faire 2 caractères minimum et 20 maximum")
        //JPA
        @Column(name = "marque", nullable = false, length = 20)
        private String marque;

        //LBK
        @Getter
        @Setter
        //BV
        @NotBlank(message = "modele ne peut pas être vide")
        @Size(min = 2, max = 20, message = "modele doit faire 2 caractères minimum et 20 maximum")
        //JPA
        @Column(name = "modele", nullable = false, length = 20)
        private String modele;
        
        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "date d'ajout ne peut pas être null")
        //JPA
        @Column(name = "date_ajout", nullable = false)
        private LocalDate dataAjout;

        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "date de fin de garantie ne peut pas être null")
        //JPA
        @Column(name = "date_fin_garantie", nullable = false)
        private LocalDate dateFinGarantie;

        //LBK
        @Getter
        @Setter
        //BV
        @NotNull(message = "l'état ne peut pas être null")
        //JPA
        @Enumerated(value = EnumType.STRING)
        @Column(name = "etat", nullable = false, length = 15)
        private EtatMateriel etat;
}