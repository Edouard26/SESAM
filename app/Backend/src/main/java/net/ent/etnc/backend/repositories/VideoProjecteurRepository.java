package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.UniteCentrale;
import net.ent.etnc.backend.models.entities.VideoProjecteur;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository permettant d'effectuer les opérations de persistance
 * et de gestion des entités {@link VideoProjecteur}.
 *
 * @see VideoProjecteur
 */
@Repository
public interface VideoProjecteurRepository extends BaseRepository<VideoProjecteur> {
}