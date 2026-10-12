package org.tp.sesam.services;

import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Contrat métier appelé par les contrôleurs. */
public interface TransfertService {
    /** Import depuis la source externe. À surcharger dans TransfertServiceImpl. */
    default ImportResponse importer(String resource, ImportRequest dto, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("TransfertService.importer");
    }

    /** Export filtré sans secrets et avec contrôle des droits. À surcharger dans TransfertServiceImpl. */
    default ExportFile exporter(String resource, org.springframework.util.MultiValueMap<String,String> filtres, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("TransfertService.exporter");
    }

}
