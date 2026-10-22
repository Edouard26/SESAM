package net.ent.etnc.backend.services;


import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service métier pour la gestion des utilisateurs.
 */
public interface UserService extends Service<User, Long> {

    @Transactional(readOnly = true)
    Boolean existsByNid(String nid);
}
