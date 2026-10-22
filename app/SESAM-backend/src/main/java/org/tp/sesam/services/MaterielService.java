package org.tp.sesam.services;

import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Contrat métier appelé par les contrôleurs. */
public interface MaterielService extends org.tp.sesam.services.commons.Service<Materiel, Long> {
    /** Recherche des matériels avec filtrage et droits. À surcharger dans MaterielServiceImpl. */
    default Page<Materiel> rechercher(MaterielFilter filtre, Pageable pageable, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("MaterielService.rechercher");
    }

    /** Consultation avec contrôle des droits sur le matériel. À surcharger dans MaterielServiceImpl. */
    default Materiel consulter(Long materialId, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("MaterielService.consulter");
    }

    /** Création avec salle obligatoire. À surcharger dans MaterielServiceImpl. */
    default Materiel creer(MaterielCreation dto, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("MaterielService.creer");
    }

    /** Modification partielle avec contrôle de version. À surcharger dans MaterielServiceImpl. */
    default Materiel modifier(Long materialId, MaterielPatch dto, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("MaterielService.modifier");
    }

    /** Affectation atomique des matériels à une salle. À surcharger dans MaterielServiceImpl. */
    default void affecter(AffectationMateriels dto, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("MaterielService.affecter");
    }

}
