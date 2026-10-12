package org.tp.sesam.services;

import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Contrat métier appelé par les contrôleurs. */
public interface StageService extends org.tp.sesam.services.commons.Service<Stage, Long> {
    /** Recherche des stages accessibles. À surcharger dans StageServiceImpl. */
    default Page<Stage> rechercher(StageFilter filtre, Pageable pageable, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("StageService.rechercher");
    }

    /** Consultation du stage accessible. À surcharger dans StageServiceImpl. */
    default Stage consulter(Long stageId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("StageService.consulter");
    }

    /** Modification et réservation atomique de salle. À surcharger dans StageServiceImpl. */
    default Stage modifier(Long stageId, StagePatch dto, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("StageService.modifier");
    }

    /** Affectation des utilisateurs actifs sans conflits. À surcharger dans StageServiceImpl. */
    default void affecterUsers(Long stageId, AffectationUsers dto, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("StageService.affecterUsers");
    }

    /** Clôture du stage. À surcharger dans StageServiceImpl. */
    default Stage cloturer(Long stageId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("StageService.cloturer");
    }

    /** Relance de notification SAGAIE. À surcharger dans StageServiceImpl. */
    default NotificationResponse relancerSagaie(Long stageId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("StageService.relancerSagaie");
    }

}
