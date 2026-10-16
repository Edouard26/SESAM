package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository JPA pour l'entité {@link User}.
 */
@Repository
public interface UserRepository extends BaseRepository<User> {

    /** Utilisé par UserDetailsServiceImpl pour charger un User depuis son username. */
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
