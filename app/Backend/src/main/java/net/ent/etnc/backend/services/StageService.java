package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.transaction.annotation.Transactional;

public interface StageService extends Service<Stage, Long> {

    @Transactional(readOnly = true)
    Boolean existsByCode(String code);
}