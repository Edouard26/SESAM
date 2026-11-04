package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.UniteCentrale;
import net.ent.etnc.backend.repositories.UniteCentraleRepository;
import net.ent.etnc.backend.services.UniteCentraleService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implémentation du service de gestion des unités centrales.
 */
@Service
public class UniteCentraleServiceImpl extends AbstractService<UniteCentrale, UniteCentraleRepository> implements UniteCentraleService {

    /**
     * Constructeur du service des unités centrales.
     *
     * @param repository le repository permettant d'accéder aux données des unités centrales
     */
    @Autowired
    public UniteCentraleServiceImpl(UniteCentraleRepository repository) {
        super(repository);
    }
}