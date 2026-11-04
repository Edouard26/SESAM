package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface définissant les opérations métier liées à {@link Stage}.
 * <p>
 * Elle étend {@link Service} afin de bénéficier des opérations CRUD génériques
 * et ajoute des méthodes spécifiques à la vérification, au démarrage et à la
 * clôture des stages.
 */
public interface StageService extends Service<Stage, Long> {

    /**
     * Vérifie si un stage existe avec le code fourni.
     *
     * @param code le code du stage à rechercher
     * @return {@code true} si un stage possède ce code, {@code false} sinon
     */
    @Transactional(readOnly = true)
    Boolean existsByCode(String code);

    /**
     * Clôture le stage fourni.
     *
     * @param stage le stage à clôturer
     */
    @Transactional
    void cloturer(Stage stage);

    /**
     * Démarre les stages concernés par le processus de démarrage.
     */
    @Transactional
    void demarrerStage();

    /**
     * Clôture automatiquement les stages dont la période est terminée.
     */
    @Transactional
    void cloturerStagesTermines();
}