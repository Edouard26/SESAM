package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.transaction.annotation.Transactional;

public interface SalleService extends Service<Salle, Long> {

    @Transactional(readOnly = true)
    Salle findByStagiaire(User stagiaire);
}