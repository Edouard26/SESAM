package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.UniteCentrale;
import net.ent.etnc.backend.repositories.UniteCentraleRepository;
import net.ent.etnc.backend.services.UniteCentraleService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UniteCentraleServiceImpl extends AbstractService<UniteCentrale, UniteCentraleRepository> implements UniteCentraleService {

    @Autowired
    public UniteCentraleServiceImpl(UniteCentraleRepository repository) {
        super(repository);
    }
}