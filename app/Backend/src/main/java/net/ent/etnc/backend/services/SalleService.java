package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

public interface SalleService extends Service<Salle, Long> {

    @Transactional(readOnly = true)
    Page<Salle> findAllOrderByAlertesEnAttente(Specification<Salle> specification, Pageable pageable);

    @Transactional(readOnly = true)
    Salle findByStagiaire(User stagiaire);

    @Transactional(readOnly = true)
    Page<Salle> findSallesDisponiblePourStage(Pageable pageable, Stage stage);

    @Transactional(readOnly = true)
    Page<Salle> findAllByBatimentId(Specification<Salle> spec, Pageable pageable, Long batimentId);
}