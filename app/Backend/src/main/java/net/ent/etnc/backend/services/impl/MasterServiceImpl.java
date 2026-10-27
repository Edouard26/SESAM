package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Master;
import net.ent.etnc.backend.repositories.MasterRepository;
import net.ent.etnc.backend.services.MasterService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MasterServiceImpl extends AbstractService<Master, MasterRepository> implements MasterService {

    @Autowired
    public MasterServiceImpl(MasterRepository repository) {
        super(repository);
    }
}
