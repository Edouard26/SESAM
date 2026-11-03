package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.transaction.annotation.Transactional;

public interface StageService extends Service<Stage, Long> {

    @Transactional(readOnly = true)
    Boolean existsByCode(String code);

    /**
     * Clôture un stage: le stage est retiré de sa salle, les matériels affectés à ses stagiaires
     * sont libérés puis les comptes des stagiaires sont désactivés.
     */
    @Transactional
    void cloturer(Stage stage);

    /** Clôture tous les stages dont la date de fin est dépassée. */
    @Transactional
    void cloturerStagesTermines();
}