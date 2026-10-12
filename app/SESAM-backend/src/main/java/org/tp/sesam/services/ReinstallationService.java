package org.tp.sesam.services;

import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Contrat métier appelé par les contrôleurs. */
public interface ReinstallationService {
    /** Démarrage asynchrone de réinstallation de salle. À surcharger dans ReinstallationServiceImpl. */
    default ReinstallationResponse lancerSalle(Long roomId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("ReinstallationService.lancerSalle");
    }

    /** Démarrage asynchrone de réinstallation de matériel. À surcharger dans ReinstallationServiceImpl. */
    default ReinstallationResponse lancerMateriel(Long materialId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("ReinstallationService.lancerMateriel");
    }

    /** Suivi de réinstallation accessible. À surcharger dans ReinstallationServiceImpl. */
    default ReinstallationResponse consulter(Long reinstallationId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("ReinstallationService.consulter");
    }

}
