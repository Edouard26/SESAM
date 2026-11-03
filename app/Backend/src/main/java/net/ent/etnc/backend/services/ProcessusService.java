package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Processus;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public interface ProcessusService extends Service<Processus, Long> {
    @Transactional(readOnly = true)
    Page<Processus> findAllByUC(Pageable pageable, Long uniteCentralId);
}
