package org.tp.sesam.services;

import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Contrat métier appelé par les contrôleurs. */
public interface SalleService extends org.tp.sesam.services.commons.Service<Salle, Long> {
    /** Recherche de salles avec filtrage et droits. À surcharger dans SalleServiceImpl. */
    default Page<Salle> rechercher(SalleFilter filtre, Pageable pageable, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("SalleService.rechercher");
    }

    /** Consultation avec contrôle des droits sur la salle. À surcharger dans SalleServiceImpl. */
    default Salle consulter(Long roomId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("SalleService.consulter");
    }

}
