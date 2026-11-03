package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.entities.Regle;
import net.ent.etnc.backend.repositories.SalleRepository;
import net.ent.etnc.backend.repositories.RegleRepository;
import net.ent.etnc.backend.services.SalleService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.HashSet;

@Service
public class SalleServiceImpl extends AbstractService<Salle, SalleRepository> implements SalleService {

    private final RegleRepository regleRepository;

    @Autowired
    public SalleServiceImpl(SalleRepository repository, RegleRepository regleRepository) {
        super(repository);
        this.regleRepository = regleRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Salle> findAllOrderByAlertesEnAttente(Specification<Salle> specification, Pageable pageable){
        return repository.findAllOrderByAlertesEnAttente(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Salle findByStagiaire(User stagiaire){
        Optional<Salle> optionalSalle = repository.findByStagiaire(stagiaire);
        if(optionalSalle.isEmpty()){
            throw new ServiceException("Salle not found");
        }
        return optionalSalle.get();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Salle> findSallesDisponiblePourStage(Pageable pageable, Stage stage){
        return repository.findSallesDisponiblePendantPeriode(pageable, stage.getDateDebut(), stage.getDateFin());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Salle> findAllByBatimentId(Specification<Salle> spec, Pageable pageable, Long batimentId){
        return repository.findAllByBatimentId(spec, pageable, batimentId);
    }

    @Transactional
    @Override
    public Salle updateRegles(Long salleId, List<Long> reglesIds) {
        Salle salle = repository.findById(salleId)
                .orElseThrow(() -> new ServiceException("Salle introuvable."));
        List<Regle> regles = regleRepository.findAllById(reglesIds == null ? List.of() : reglesIds);
        if (regles.size() != (reglesIds == null ? 0 : new HashSet<>(reglesIds).size())) {
            throw new ServiceException("Une ou plusieurs règles sont introuvables.");
        }
        salle.setRegles(new HashSet<>(regles));
        return repository.save(salle);
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean existsSalleLibreDurantPeriode(Long salleId, LocalDate dateDebut, LocalDate dateFin){
        return repository.existsSalleLibreDurantPeriode(salleId, dateDebut, dateFin);
    }
}
