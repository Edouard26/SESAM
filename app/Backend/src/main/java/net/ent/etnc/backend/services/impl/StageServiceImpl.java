package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.repositories.StageRepository;
import net.ent.etnc.backend.services.StageService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StageServiceImpl extends AbstractService<Stage, StageRepository> implements StageService {

    @Autowired
    public StageServiceImpl(StageRepository repository) {
        super(repository);
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean existsByCode(String code){
        return repository.existsByCode(code);
    }
}