package net.ent.etnc.backend.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.ent.etnc.backend.models.commons.AbstractPersistableWithIdSetter;

@Entity
@Table(
        name = "processus",
        uniqueConstraints = {@UniqueConstraint(
                name = "uk___processus___nom",
                columnNames = {"nom"}
        )}
)
@EqualsAndHashCode(callSuper = false, of = {"nom"})
@ToString(callSuper = true, of = {"nom"})
public class Processus extends AbstractPersistableWithIdSetter<Long> {

    //LBK
    @Getter
    @Setter
    //BV
    @NotBlank(message = "nom du processus ne peut pas être vide")
    @Size(max = 30, message = "nom du processus doit faire 30 caractères maximum")
    //JPA
    @Column(name = "nom", nullable = false, length = 30)
    private String nom;
}
