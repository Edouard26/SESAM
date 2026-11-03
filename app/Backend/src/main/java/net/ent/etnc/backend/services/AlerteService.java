package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

public interface AlerteService extends Service<Alerte, Long> {

    @Transactional(readOnly = true)
    Alerte findById(User user, Long alerteId);

    @Transactional(readOnly = true)
    Page<Alerte> findAllByUserId(Pageable pageable, Long userId);

    @Transactional(readOnly = true)
    Integer countAlerteInSalle(Long salleId);

    @Transactional(readOnly = true)
    Page<Alerte> findAllBySalleId(Pageable pageable, Long salleId);
}