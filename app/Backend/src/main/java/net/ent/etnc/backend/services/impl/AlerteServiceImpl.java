package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.repositories.AlerteRepository;
import net.ent.etnc.backend.services.AlerteService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlerteServiceImpl extends AbstractService<Alerte, AlerteRepository> implements AlerteService {

    @Autowired
    public AlerteServiceImpl(AlerteRepository repository) {
        super(repository);
    }

    @Transactional(readOnly = true)
    @Override
    public Integer countAlerteInSalle(Long salleId){
        return repository.countAlerteInSalle(salleId);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Alerte> findAllBySalleId(Pageable pageable, Long salleId){
        return repository.findAllBySalleId(pageable, salleId);
    }
}