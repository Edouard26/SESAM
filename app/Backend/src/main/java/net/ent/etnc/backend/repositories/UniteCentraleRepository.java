package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.UniteCentrale;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository permettant d'effectuer les opérations de persistance
 * et de gestion des entités {@link UniteCentrale}.
 *
 * @see UniteCentrale
 */
@Repository
public interface UniteCentraleRepository extends BaseRepository<UniteCentrale> {
}