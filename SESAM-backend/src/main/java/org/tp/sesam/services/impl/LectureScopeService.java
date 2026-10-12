package org.tp.sesam.services.impl;

import org.tp.sesam.models.enums.Role;
import org.tp.sesam.repositories.UserRepository;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/** Contrôle provisoire explicite, en attendant les relations User/Stage/Salle. */
@Service
public class LectureScopeService {
    private final UserRepository users;
    public LectureScopeService(UserRepository users) { this.users = users; }

    public void verifierLectureGlobale(Long actorId) {
        var actor = users.findById(actorId).orElseThrow(() -> new AccessDeniedException("Compte introuvable"));
        if (!actor.isActif()) throw new AccessDeniedException("Compte inactif");
        if (actor.getRole() != Role.ADMINISTRATEUR && actor.getRole() != Role.GESTIONNAIRE) {
            // Ne jamais exposer toutes les ressources à un stagiaire ou formateur
            // tant que les relations permettant de déterminer son périmètre sont absentes.
            throw new FonctionnaliteNonImplementeeException("Filtrage du périmètre User/Stage/Salle");
        }
    }
}
