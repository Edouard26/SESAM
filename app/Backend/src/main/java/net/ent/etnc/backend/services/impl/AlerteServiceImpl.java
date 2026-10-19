package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.repositories.AlerteRepository;
import net.ent.etnc.backend.services.AlerteService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlerteServiceImpl extends AbstractService<Alerte, AlerteRepository> implements AlerteService {

    @Autowired
    public AlerteServiceImpl(AlerteRepository repository) {
        super(repository);
    }
}