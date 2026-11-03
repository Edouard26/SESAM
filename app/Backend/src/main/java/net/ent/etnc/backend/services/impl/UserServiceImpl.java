package net.ent.etnc.backend.services.impl;


import net.ent.etnc.backend.dtos.requests.MaterielAssigmentsStagiaireRequest;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.enums.Role;
import net.ent.etnc.backend.repositories.UserRepository;
import net.ent.etnc.backend.services.MaterielService;
import net.ent.etnc.backend.services.SalleService;
import net.ent.etnc.backend.services.UserService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    private final MaterielService materielService;
    private final SalleService salleService;

    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, MaterielService materielService, SalleService salleService) {
        super(repository);
        this.passwordEncoder = passwordEncoder;
        this.materielService = materielService;
        this.salleService = salleService;
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

    @Transactional(readOnly = true)
    @Override
    public User findByNid(String nid){
        Optional<User> optional = repository.findByNid(nid);
        if(optional.isEmpty()){
            throw new ServiceException("user Not found");
        }

        return optional.get();
    }

    @Transactional
    @Override
    public User AssigmentsMaterielToStagiaire(MaterielAssigmentsStagiaireRequest request){
        Optional<User> optionalStagiaire = findById(request.getStagiaireId());
        if(optionalStagiaire.isEmpty() || !optionalStagiaire.get().isActive()){
            throw new ServiceException("User not found");
        }

        if(optionalStagiaire.get().getRole() != Role.STAGIAIRE){
            throw new ServiceException("User is not a Stagiaire");
        }

        for(Long materielId : request.getMaterielsId()){
            Optional<Materiel> optionalMateriel = materielService.findById(materielId);
            // RG: le matériel ne peut pas déjà être affecter un autre stagiaire
            // RG: le matériel doit être dans la salle du stage du stagiaire
            if(optionalMateriel.isPresent() && materielService.isAssignableToStagiaire(optionalMateriel.get(), salleService.findByStagiaire(optionalStagiaire.get()))){
                optionalMateriel.get().setStagiaire(optionalStagiaire.get());
            }
        }

        return update(optionalStagiaire.get());
    }
}
