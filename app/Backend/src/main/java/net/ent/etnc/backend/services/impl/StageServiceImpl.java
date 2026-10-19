package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.repositories.StageRepository;
import net.ent.etnc.backend.services.StageService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StageServiceImpl extends AbstractService<Stage, StageRepository> implements StageService {

    @Autowired
    public StageServiceImpl(StageRepository repository) {
        super(repository);
    }
}