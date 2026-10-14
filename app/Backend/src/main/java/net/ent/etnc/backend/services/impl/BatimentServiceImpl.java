package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Batiment;
import net.ent.etnc.backend.repositories.BatimentRepository;
import net.ent.etnc.backend.services.BatimentService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BatimentServiceImpl extends AbstractService<Batiment, BatimentRepository> implements BatimentService {

    @Autowired
    public BatimentServiceImpl(BatimentRepository repository) {
        super(repository);
    }

    @Transactional(readOnly = true)
    @Override
    public String findNomBySalleId(Long salleId){
        Optional<Batiment> batiment = repository.findBySallesId(salleId);
        if(batiment.isEmpty())
            throw new ServiceException("Batiment Not found with salleId");
        return batiment.get().getNom();
    }
}