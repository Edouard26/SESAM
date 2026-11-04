package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Regle;
import net.ent.etnc.backend.repositories.RegleRepository;
import net.ent.etnc.backend.repositories.SalleRepository;
import net.ent.etnc.backend.services.RegleService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service chargé de la gestion des règles associées aux salles.
 *
 * @see RegleService
 * @see RegleRepository
 * @see Regle
 */
@Service
public class RegleServiceImpl extends AbstractService<Regle, RegleRepository> implements RegleService {

    /**
     * Repository permettant d'accéder aux salles et de gérer
     * leurs associations avec les règles.
     */
    private final SalleRepository salleRepository;

    /**
     * Initialise le service avec les repositories nécessaires
     * à la gestion des règles et des salles.
     *
     * @param repository repository des règles
     * @param salleRepository repository des salles
     */
    @Autowired
    public RegleServiceImpl(RegleRepository repository, SalleRepository salleRepository) {
        super(repository);
        this.salleRepository = salleRepository;
    }

    /**
     * Supprime une règle après avoir retiré ses associations avec les salles.
     *
     * <p>Recherche toutes les salles associées à la règle identifiée,
     * retire cette règle de leur collection de règles, puis enregistre
     * les modifications de chaque salle. Enfin, délègue la suppression
     * de la règle à la méthode {@code deleteById} de la classe parente.</p>
     *
     * @param id identifiant de la règle à supprimer
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        // Recherche toutes les salles associées à la règle.
        salleRepository.findAllByRegles_Id(id).forEach(salle -> {
            // Retire de la collection de la salle la règle correspondant à l'identifiant.
            salle.getRegles().removeIf(regle -> regle.getId().equals(id));

            // Enregistre les modifications apportées à la salle.
            salleRepository.save(salle);
        });

        // Supprime la règle en utilisant le comportement défini dans la classe parente.
        super.deleteById(id);
    }
}
