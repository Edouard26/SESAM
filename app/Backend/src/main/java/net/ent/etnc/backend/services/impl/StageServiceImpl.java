package net.ent.etnc.backend.services.impl;

import jakarta.validation.groups.Default;
import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.models.validators.groups.UpdateGroup;
import net.ent.etnc.backend.repositories.StageRepository;
import net.ent.etnc.backend.services.StageService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
public class StageServiceImpl extends AbstractService<Stage, StageRepository> implements StageService {

    @Autowired
    public StageServiceImpl(StageRepository repository) {
        super(repository);
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean existsByCode(String code){
        return repository.existsByCode(code);
    }

    @Transactional
    @Override
    @Validated({Default.class, UpdateGroup.class})
    public Stage update(Stage stage) throws ServiceException {
        if (stage.getId() == null) {
            throw new ServiceException("L'ID du stage ne peut pas être null pour une mise à jour.");
        }
        if (!repository.existsById(stage.getId())) {
            throw new ServiceException("Le stage avec l'ID " + stage.getId() + " n'existe pas.");
        }
        try {
            // RG -> La salle destinée à accueillir le stage doit être disponible pendant toute sa durée.
            if(repository.existsSalleLibreDurantPeriode(stage.getSalleCours().getId(), stage.getDateDebut(), stage.getDateFin())){
                throw new ServiceException("Salle " + stage.getSalleCours().getNom() + "non-disponible");
            }
            return repository.save(stage);
        } catch (Exception e) {
            throw new ServiceException("Erreur lors de la mise à jour de l'entité", e);
        }
    }
}