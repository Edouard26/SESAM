package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Ecran;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository permettant d'effectuer les opérations de persistance
 * et de recherche sur l'entité {@link Ecran}.
 *
 * @see Ecran
 */
@Repository
public interface EcranRepository extends BaseRepository<Ecran> {
}