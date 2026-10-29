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

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "masters",
        uniqueConstraints = {@UniqueConstraint(
                name = "uk___master___nom",
                columnNames = {"nom"}
        )}
)
@EqualsAndHashCode(callSuper = false, of = {"nom"})
@ToString(callSuper = true, of = {"nom"})
public class Master extends AbstractPersistableWithIdSetter<Long> {
    //LBK
    @Getter
    @Setter
    //BV
    @NotBlank(message = "nom de la règle ne peut pas être vide")
    @Size(max = 20, message = "nom de la règle doit faire 20 caractères maximum")
    //JPA
    @Column(name = "nom", nullable = false, length = 20)
    private String nom;

    //LBK
    @Getter
    @Setter
    //BV
    @Valid
    //JPA
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "masters_processus",
            joinColumns = @JoinColumn(name = "master_id", foreignKey = @ForeignKey(name = "fk___masters_processus___regle_id")),
            inverseJoinColumns = @JoinColumn(name = "processus_id", foreignKey = @ForeignKey(name = "fk___masters_processus___processus_id"))
    )
    private List<Processus> processuss = new ArrayList<>();
}
