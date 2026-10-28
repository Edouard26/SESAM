package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Processus;
import net.ent.etnc.backend.repositories.ProcessusRepository;
import net.ent.etnc.backend.services.ProcessusService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.stereotype.Service;

@Service
public class ProcessusServiceImpl extends AbstractService<Processus, ProcessusRepository> implements ProcessusService {
    public ProcessusServiceImpl(ProcessusRepository repository) {
        super(repository);
    }
}
