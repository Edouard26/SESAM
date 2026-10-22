package org.tp.sesam.services;

import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Contrat métier appelé par les contrôleurs. */
public interface AlerteService {
    /** Recherche des alertes accessibles. À surcharger dans AlerteServiceImpl. */
    default Page<AlerteResponse> rechercher(AlerteFilter filtre, Pageable pageable, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("AlerteService.rechercher");
    }

    /** Signalement des matériels accessibles. À surcharger dans AlerteServiceImpl. */
    default AlerteResponse creer(AlerteCreation dto, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("AlerteService.creer");
    }

    /** Traitement du signalement avec contrôle de version. À surcharger dans AlerteServiceImpl. */
    default AlerteResponse modifier(Long alertId, AlertePatch dto, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("AlerteService.modifier");
    }

}
