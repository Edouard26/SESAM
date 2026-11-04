package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Master;
import net.ent.etnc.backend.repositories.MasterRepository;
import net.ent.etnc.backend.services.MasterService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service chargé de la gestion des masters.
 *
 * @see MasterService
 * @see MasterRepository
 * @see Master
 */
@Service
public class MasterServiceImpl extends AbstractService<Master, MasterRepository> implements MasterService {

    /**
     * Initialise le service avec le repository des masters.
     *
     * @param repository repository permettant d'accéder aux données des masters
     */
    @Autowired
    public MasterServiceImpl(MasterRepository repository) {
        super(repository);
    }
}
