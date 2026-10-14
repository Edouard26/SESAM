package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.repositories.MaterielRepository;
import net.ent.etnc.backend.services.MaterielService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.stereotype.Service;

@Service
public class MaterielServiceImpl extends AbstractService<Materiel, MaterielRepository> implements MaterielService {

    public MaterielServiceImpl(MaterielRepository repository) {
        super(repository);
    }
}
