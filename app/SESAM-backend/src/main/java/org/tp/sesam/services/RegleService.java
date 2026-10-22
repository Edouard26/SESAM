package org.tp.sesam.services;

import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Contrat métier appelé par les contrôleurs. */
public interface RegleService {
    /** Règles de salle du formateur connecté. À surcharger dans RegleServiceImpl. */
    default List<RegleResponse> listerSalle(Long roomId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("RegleService.listerSalle");
    }

    /** Création ou remplacement de la règle de salle du formateur. À surcharger dans RegleServiceImpl. */
    default RegleResponse modifierSalle(Long roomId, Long processId, ReglePatch dto, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("RegleService.modifierSalle");
    }

    /** Suppression de la règle de salle du formateur. À surcharger dans RegleServiceImpl. */
    default void supprimerSalle(Long roomId, Long processId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("RegleService.supprimerSalle");
    }

    /** Suppression des seules règles du formateur dans cette salle. À surcharger dans RegleServiceImpl. */
    default void supprimerToutesSalle(Long roomId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("RegleService.supprimerToutesSalle");
    }

    /** Règles de référence du stage. À surcharger dans RegleServiceImpl. */
    default List<RegleResponse> listerStage(Long stageId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("RegleService.listerStage");
    }

    /** Création ou remplacement de la règle de stage. À surcharger dans RegleServiceImpl. */
    default RegleResponse modifierStage(Long stageId, Long processId, ReglePatch dto, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("RegleService.modifierStage");
    }

    /** Suppression de la règle de stage. À surcharger dans RegleServiceImpl. */
    default void supprimerStage(Long stageId, Long processId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("RegleService.supprimerStage");
    }

}
