package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.entities.VideoProjecteur;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository permettant d'effectuer les opérations de persistance
 * et de gestion des entités {@link User}.
 *
 * @see User
 */
@Repository
public interface UserRepository extends BaseRepository<User> {

    /** Utilisé par UserDetailsServiceImpl pour charger un User depuis son username. */
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByNid(String nid);

    Optional<User> findByNid(String nid);
}
