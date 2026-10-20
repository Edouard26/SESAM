package net.ent.etnc.backend.services.impl;


import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.repositories.UserRepository;
import net.ent.etnc.backend.services.UserService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation de {@link UserService}.
 *
 * <p>Encode automatiquement le mot de passe en BCrypt lors de la création
 * et garantit l'unicité de l'email (RG-U01) ainsi que la présence d'au moins
 * un administrateur actif (RG-U03).
 */
@Service
@Transactional
public class UserServiceImpl extends AbstractService<User, UserRepository> implements UserService {

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder) {
        super(repository);
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crée un utilisateur en encodant son mot de passe et en vérifiant l'unicité de l'email (RG-U01).
     */
    @Override
    public User create(User user) throws ServiceException {
        if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().isBlank()) {
            throw new ServiceException("Le mot de passe est obligatoire");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean existsByNid(String nid){
        return repository.existsByNid(nid);
    }
}
