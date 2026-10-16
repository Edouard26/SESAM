package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.repositories.SalleRepository;
import net.ent.etnc.backend.services.SalleService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SalleServiceImpl extends AbstractService<Salle, SalleRepository> implements SalleService {

    @Autowired
    public SalleServiceImpl(SalleRepository repository) {
        super(repository);
    }
}