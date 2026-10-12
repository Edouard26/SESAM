package org.tp.sesam.services;

import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Contrat métier appelé par les contrôleurs. */
public interface DashboardService {
    /** Tableau de bord adapté au rôle et au périmètre. À surcharger dans DashboardServiceImpl. */
    default DashboardResponse consulter(Long actorId) {
        throw new FonctionnaliteNonImplementeeException("DashboardService.consulter");
    }

}
