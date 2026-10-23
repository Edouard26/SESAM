package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface MaterielService extends Service<Materiel, Long> {
    @Transactional(readOnly = true)
    Page<Materiel> findAllBySalleId(Specification<Materiel> spec, Pageable pageable, Long salleId, Boolean excludeStagiaire);

    @Transactional(readOnly = true)
    Page<Materiel> findAllByExcludedSalleId(Specification<Materiel> spec, Pageable pageable, Long salleId);

    @Transactional(readOnly = true)
    Boolean existsByNumeroSerie(String numeroSerie);

    @Transactional(readOnly = true)
    Boolean isAssignableToStagiaire(Materiel materiel, Salle salle);

    @Transactional(readOnly = true)
    Page<Materiel> findAllMaterielAffectToStagiaire(User stagiaire, Pageable pageable);

    @Transactional(readOnly = true)
    List<Materiel> findAllByIdIn(Collection<Long> ids);
}
