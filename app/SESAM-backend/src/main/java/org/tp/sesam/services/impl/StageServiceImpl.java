package org.tp.sesam.services.impl;

import org.tp.sesam.models.entities.*;
import org.tp.sesam.repositories.StageRepository;
import org.tp.sesam.services.StageService;
import org.tp.sesam.services.commons.AbstractService;
import org.tp.sesam.services.commons.ResourceNotFoundException;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.tp.sesam.dtos.api.ApiDtos.StageFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class StageServiceImpl extends AbstractService<Stage, StageRepository>
        implements StageService {
    private final LectureScopeService scope;
    public StageServiceImpl(StageRepository repository, LectureScopeService scope) {
        super(repository);
        this.scope = scope;
    }

    @Override
    public Page<Stage> rechercher(StageFilter filtre, Pageable pageable, Long actorId) {
        scope.verifierLectureGlobale(actorId);
        // Aucun filtre n'est ignoré : ceux qui exigent des champs encore absents sont signalés.
        if (filtre.status() != null || filtre.roomId() != null) {
            throw new FonctionnaliteNonImplementeeException("Filtres nécessitant les relations ou états de Stage");
        }
        return repository.findAll((entity, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (filtre.search() != null && !filtre.search().isBlank()) {
                String escaped = filtre.search().strip().toLowerCase(Locale.ROOT)
                        .replace("!", "!!").replace("%", "!%").replace("_", "!_");
                String pattern = "%" + escaped + "%";
                predicates.add(cb.like(cb.lower(entity.get("nom")), pattern, '!'));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable);
    }

    @Override
    public Stage consulter(Long id, Long actorId) {
        scope.verifierLectureGlobale(actorId);
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Stage introuvable : " + id));
    }

    // Les autres cas métier sont à surcharger ici ; leurs contrats retournent 501 pour l'instant.
}
