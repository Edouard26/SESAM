package net.ent.etnc.backend.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.ent.etnc.backend.models.commons.AbstractPersistableWithIdSetter;
import net.ent.etnc.backend.models.enums.TypeMateriel;

/**
 * Représente un vidéo projecteur.
 */
@Entity
@EqualsAndHashCode(callSuper = true, of = {})
@ToString(callSuper = true, of = {})
public class VideoProjecteur extends Materiel {

    //LBK
    @Getter
    @Setter
    //BV
    @NotNull(message = "type ne peut pas être null")
    //JPA
    @Enumerated(value = EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TypeMateriel type = TypeMateriel.VIDEO_PROJECTEUR;
}