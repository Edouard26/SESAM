package net.ent.etnc.backend.models.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.ent.etnc.backend.models.commons.AbstractPersistableWithIdSetter;

/**
 * Représente un vidéo projecteur.
 */
@Entity
@EqualsAndHashCode(callSuper = true, of = {})
@ToString(callSuper = true, of = {})
public class VideoProjecteur extends Materiel {

}