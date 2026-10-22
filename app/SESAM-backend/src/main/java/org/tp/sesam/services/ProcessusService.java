package org.tp.sesam.services;

import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Contrat métier appelé par les contrôleurs. */
public interface ProcessusService extends org.tp.sesam.services.commons.Service<Processus, Long> {
    /** Processus observés dans la salle et règles effectives. À surcharger dans ProcessusServiceImpl. */
    default List<ProcessusObserve> observerSalle(Long roomId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("ProcessusService.observerSalle");
    }

    /** Processus observés sur le matériel et règles effectives. À surcharger dans ProcessusServiceImpl. */
    default List<ProcessusObserve> observerMateriel(Long materialId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("ProcessusService.observerMateriel");
    }

}
