package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.enums.Role;
import net.ent.etnc.backend.models.enums.StatutAlerte;
import net.ent.etnc.backend.repositories.AlerteRepository;
import net.ent.etnc.backend.services.AlerteService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Transactional(readOnly = true)
    @Override
    public Alerte findById(User user, Long alerteId){
        Optional<Alerte> optionalAlerte = repository.findById(alerteId);
        if(optionalAlerte.isEmpty()){
            throw new ServiceException("Alerte not found");
        }

        // RG -> Un stagiaire ne peut voir que ses alertes.
        if(user.getRole() != Role.STAGIAIRE || !optionalAlerte.get().getSignaleur().equals(user)){
            throw new ServiceException("l'user n'est pas le signaleur de l'alerte");
        }

        return optionalAlerte.get();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Alerte> findAllByUserId(Pageable pageable, Long userId){
        return repository.findAllByUserId(pageable, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public Integer countAlerteInSalle(Long salleId){
        return repository.countAlerteInSalle(salleId);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Alerte> findAllBySalleId(Pageable pageable, Long salleId){
        return repository.findAllBySalleId(pageable, salleId);
    }
}