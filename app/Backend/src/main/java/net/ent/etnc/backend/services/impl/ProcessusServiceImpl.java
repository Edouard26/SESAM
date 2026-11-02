package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Processus;
import net.ent.etnc.backend.repositories.ProcessusRepository;
import net.ent.etnc.backend.services.ProcessusService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ProcessusServiceImpl extends AbstractService<Processus, ProcessusRepository> implements ProcessusService {
    public ProcessusServiceImpl(ProcessusRepository repository) {
        super(repository);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Processus> findAllByUC(Pageable pageable, Long uniteCentralId){
        return repository.findAllByUC(pageable, uniteCentralId);
    }
}
