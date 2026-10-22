package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.repositories.MaterielRepository;
import net.ent.etnc.backend.services.MaterielService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterielServiceImpl extends AbstractService<Materiel, MaterielRepository> implements MaterielService {

    public MaterielServiceImpl(MaterielRepository repository) {
        super(repository);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Materiel> findAllBySalleId(Specification<Materiel> spec, Pageable pageable, Long salleId){
        return repository.findAllBySalleId(spec, pageable, salleId);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Materiel> findAllByExcludedSalleId(Specification<Materiel> spec, Pageable pageable, Long salleId){
        return repository.findAllByExcludedSalleId(spec, pageable, salleId);
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean existsByNumeroSerie(String numeroSerie){
        return repository.existsByNumeroSerie(numeroSerie);
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean isAssignableToStagiaire(Materiel materiel, Salle salle){
        return repository.isInSalle(materiel, salle) && !repository.isAffectedToStagiaire(materiel);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Materiel> findAllMaterielAffectToStagiaire(User stagiaire, Pageable pageable){
        return repository.findAllMaterielAffectToStagiaire(stagiaire, pageable);
    }
}
