package org.tp.sesam.models.commons;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.io.Serializable;

/**
 * Identifiant hérité et verrouillage optimiste. Ne pas régénérer equals/hashCode ici.
 */
@MappedSuperclass
public abstract class AbstractPersistableWithIdSetter<PK extends Serializable>
        extends AbstractPersistable<PK> {
    @Override
    public void setId(PK id) {
        super.setId(id);
    }

    @Version
    @Getter
    @Setter
    private Long version;
}
