package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.repositories.SalleRepository;
import net.ent.etnc.backend.services.SalleService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SalleServiceImpl extends AbstractService<Salle, SalleRepository> implements SalleService {

    @Autowired
    public SalleServiceImpl(SalleRepository repository) {
        super(repository);
    }

    @Transactional(readOnly = true)
    @Override
    public Salle findByStagiaire(User stagiaire){
        Optional<Salle> optionalSalle = repository.findByStagiaire(stagiaire);
        if(optionalSalle.isEmpty()){
            throw new ServiceException("Salle not found");
        }
        return optionalSalle.get();
    }
}