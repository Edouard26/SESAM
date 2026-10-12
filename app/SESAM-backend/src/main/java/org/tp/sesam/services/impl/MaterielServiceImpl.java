package org.tp.sesam.services.impl;

import org.tp.sesam.models.entities.*;
import org.tp.sesam.repositories.MaterielRepository;
import org.tp.sesam.services.MaterielService;
import org.tp.sesam.services.commons.AbstractService;
import org.tp.sesam.services.commons.ResourceNotFoundException;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.tp.sesam.dtos.api.ApiDtos.MaterielFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class MaterielServiceImpl extends AbstractService<Materiel, MaterielRepository>
        implements MaterielService {
    private final LectureScopeService scope;
    public MaterielServiceImpl(MaterielRepository repository, LectureScopeService scope) {
        super(repository);
        this.scope = scope;
    }

    @Override
    public Page<Materiel> rechercher(MaterielFilter filtre, Pageable pageable, Long actorId) {
        scope.verifierLectureGlobale(actorId);
        // Aucun filtre n'est ignoré : ceux qui exigent des champs encore absents sont signalés.
        if (filtre.type() != null || filtre.state() != null || filtre.connection() != null
                || filtre.roomId() != null || filtre.building() != null) {
            throw new FonctionnaliteNonImplementeeException("Filtres nécessitant les relations ou états de Materiel");
        }
        return repository.findAll((entity, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (filtre.search() != null && !filtre.search().isBlank()) {
                String escaped = filtre.search().strip().toLowerCase(Locale.ROOT)
                        .replace("!", "!!").replace("%", "!%").replace("_", "!_");
                String pattern = "%" + escaped + "%";
                predicates.add(cb.or(cb.like(cb.lower(entity.get("numeroSerie")), pattern, '!'),
                        cb.like(cb.lower(entity.get("marque")), pattern, '!')));
            }
            if (filtre.materialId() != null) predicates.add(cb.equal(entity.get("id"), filtre.materialId()));
            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable);
    }

    @Override
    public Materiel consulter(Long id, Long actorId) {
        scope.verifierLectureGlobale(actorId);
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Materiel introuvable : " + id));
    }

    // Les autres cas métier sont à surcharger ici ; leurs contrats retournent 501 pour l'instant.
}
