package org.tp.sesam.services.impl;

import org.tp.sesam.models.entities.Processus;
import org.tp.sesam.repositories.ProcessusRepository;
import org.tp.sesam.services.ProcessusService;
import org.tp.sesam.services.commons.AbstractService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class ProcessusServiceImpl extends AbstractService<Processus, ProcessusRepository>
        implements ProcessusService {
    public ProcessusServiceImpl(ProcessusRepository repository) {
        super(repository);
    }
    // TODO : surcharger create/update avec les règles et @Transactional.
    // Les cas métier portant sur plusieurs accès BDD nécessitent une transaction globale.
}
