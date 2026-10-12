package org.tp.sesam.services.impl;

import org.tp.sesam.models.entities.*;
import org.tp.sesam.repositories.SalleRepository;
import org.tp.sesam.services.SalleService;
import org.tp.sesam.services.commons.AbstractService;
import org.tp.sesam.services.commons.ResourceNotFoundException;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.tp.sesam.dtos.api.ApiDtos.SalleFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class SalleServiceImpl extends AbstractService<Salle, SalleRepository>
        implements SalleService {
    private final LectureScopeService scope;
    public SalleServiceImpl(SalleRepository repository, LectureScopeService scope) {
        super(repository);
        this.scope = scope;
    }

    @Override
    public Page<Salle> rechercher(SalleFilter filtre, Pageable pageable, Long actorId) {
        scope.verifierLectureGlobale(actorId);

        // Ces filtres nécessitent encore des champs ou relations à implémenter.
        if (filtre.building() != null || filtre.state() != null || filtre.available() != null
                || filtre.stageId() != null || filtre.startDate() != null || filtre.endDate() != null) {
            throw new FonctionnaliteNonImplementeeException(
                    "Filtres nécessitant les relations ou états de Salle"
            );
        }

        return repository.findAll((entity, query, cb) -> {
            var predicates = new ArrayList<Predicate>();

            // Recherche dans le nom, sans distinction entre majuscules et minuscules.
            if (filtre.search() != null && !filtre.search().isBlank()) {
                String escaped = filtre.search().strip().toLowerCase(Locale.ROOT)
                        .replace("!", "!!")
                        .replace("%", "!%")
                        .replace("_", "!_");

                String pattern = "%" + escaped + "%";
                predicates.add(cb.like(cb.lower(entity.get("nom")), pattern, '!'));
            }

            // Filtre sur l'attribut Java typeSalle.
            if (filtre.type() != null) {
                predicates.add(cb.equal(entity.get("typeSalle"), filtre.type()));
            }

            // Combine les conditions avec un ET logique.
            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable);
    }

    @Override
    public Salle consulter(Long id, Long actorId) {
        scope.verifierLectureGlobale(actorId);
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Salle introuvable : " + id));
    }

    // Les autres cas métier sont à surcharger ici ; leurs contrats retournent 501 pour l'instant.
}
