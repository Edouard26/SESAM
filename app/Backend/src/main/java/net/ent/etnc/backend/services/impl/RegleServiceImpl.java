package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Regle;
import net.ent.etnc.backend.repositories.RegleRepository;
import net.ent.etnc.backend.repositories.SalleRepository;
import net.ent.etnc.backend.services.RegleService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegleServiceImpl extends AbstractService<Regle, RegleRepository> implements RegleService {

    private final SalleRepository salleRepository;

    @Autowired
    public RegleServiceImpl(RegleRepository repository, SalleRepository salleRepository) {
        super(repository);
        this.salleRepository = salleRepository;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        salleRepository.findAllByRegles_Id(id).forEach(salle -> {
            salle.getRegles().removeIf(regle -> regle.getId().equals(id));
            salleRepository.save(salle);
        });
        super.deleteById(id);
    }
}
