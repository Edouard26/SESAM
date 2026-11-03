package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

public interface StageService extends Service<Stage, Long> {

    @Transactional(readOnly = true)
    Boolean existsByCode(String code);

    @Transactional
    void cloturer(Stage stage);

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    void demarrerStage();

    @Transactional
    void cloturerStagesTermines();
}