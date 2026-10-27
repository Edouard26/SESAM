package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Regle;
import net.ent.etnc.backend.repositories.RegleRepository;
import net.ent.etnc.backend.services.RegleService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegleServiceImpl extends AbstractService<Regle, RegleRepository> implements RegleService {

    @Autowired
    public RegleServiceImpl(RegleRepository repository) {
        super(repository);
    }
}
