package net.ent.etnc.backend.services;

import net.ent.etnc.backend.dtos.responses.StatsResponse;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface définissant les opérations métier liées aux statistiques.
 */
public interface StatsService {

    /**
     * Récupère les statistiques globales de l'application.
     *
     * @return un objet contenant les statistiques de l'application
     */
    @Transactional(readOnly = true)
    StatsResponse getStats();
}
