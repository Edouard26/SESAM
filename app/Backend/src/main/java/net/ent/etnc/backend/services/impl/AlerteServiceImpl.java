package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.models.enums.StatutAlerte;
import net.ent.etnc.backend.repositories.AlerteRepository;
import net.ent.etnc.backend.services.AlerteService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlerteServiceImpl extends AbstractService<Alerte, AlerteRepository> implements AlerteService {

    @Autowired
    public AlerteServiceImpl(AlerteRepository repository) {
        super(repository);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void supprimerAlertesTraiteesAnciennes() {
        LocalDateTime dateLimite = LocalDateTime.now().minusYears(1);
        List<Alerte> alertesAnciennes = repository.findByStatutAndDateResolutionBefore(
                StatutAlerte.TRAITEE,
                dateLimite
        );
        repository.deleteAll(alertesAnciennes);
    }
}
