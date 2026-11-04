package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Regle;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository permettant d'effectuer les opérations de persistance
 * et de recherche sur l'entité {@link Regle}.
 *
 * @see Regle
 */
@Repository
public interface RegleRepository extends BaseRepository<Regle> {
}
