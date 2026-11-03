package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Processus;
import net.ent.etnc.backend.models.entities.UniteCentrale;
import net.ent.etnc.backend.repositories.ProcessusRepository;
import net.ent.etnc.backend.repositories.UniteCentraleRepository;
import net.ent.etnc.backend.services.ProcessusService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class ProcessusServiceImpl extends AbstractService<Processus, ProcessusRepository> implements ProcessusService {
    private final UniteCentraleRepository uniteCentraleRepository;

    public ProcessusServiceImpl(ProcessusRepository repository, UniteCentraleRepository uniteCentraleRepository) {
        super(repository);
        this.uniteCentraleRepository = uniteCentraleRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Processus> findAllByUC(Pageable pageable, Long uniteCentralId){
        return repository.findAllByUC(pageable, uniteCentralId);
    }

    @Transactional
    @Override
    public List<Processus> updateForUC(Long uniteCentralId, List<String> noms) {
        UniteCentrale uc = uniteCentraleRepository.findById(uniteCentralId)
                .orElseThrow(() -> new ServiceException("L'unité centrale avec l'ID " + uniteCentralId + " n'existe pas."));
        List<Processus> processus = new ArrayList<>();
        for (String nom : new LinkedHashSet<>(noms)) {
            processus.add(repository.findByNom(nom).orElseGet(() -> {
                Processus nouveau = new Processus();
                nouveau.setNom(nom);
                return repository.save(nouveau);
            }));
        }
        uc.setProcessus(processus);
        uniteCentraleRepository.save(uc);
        return processus;
    }

    @Transactional
    @Override
    public void patchForUC(Long uniteCentralId, List<String> started, List<String> stopped) {
        UniteCentrale uc = uniteCentraleRepository.findById(uniteCentralId)
                .orElseThrow(() -> new ServiceException("L'unité centrale avec l'ID " + uniteCentralId + " n'existe pas."));
        uc.getProcessus().removeIf(p -> stopped.contains(p.getNom()));
        for (String nom : started) {
            if (uc.getProcessus().stream().anyMatch(p -> p.getNom().equals(nom))) continue;
            uc.getProcessus().add(repository.findByNom(nom).orElseGet(() -> {
                Processus nouveau = new Processus();
                nouveau.setNom(nom);
                return repository.save(nouveau);
            }));
        }
        uniteCentraleRepository.save(uc);
    }

    @Transactional(readOnly = true)
    @Override
    public List<String> findNomsInterditsByUC(Long uniteCentralId) {
        return repository.findNomsInterditsByUC(uniteCentralId);
    }
}
