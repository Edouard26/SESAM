package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Ecran;
import net.ent.etnc.backend.repositories.EcranRepository;
import net.ent.etnc.backend.services.EcranService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EcranServiceImpl extends AbstractService<Ecran, EcranRepository> implements EcranService {

    @Autowired
    public EcranServiceImpl(EcranRepository repository) {
        super(repository);
    }
}