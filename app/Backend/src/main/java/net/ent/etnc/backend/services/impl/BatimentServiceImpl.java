package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Batiment;
import net.ent.etnc.backend.repositories.BatimentRepository;
import net.ent.etnc.backend.services.BatimentService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implémentation du service de gestion des bâtiments.
 *
 * @see BatimentService
 * @see BatimentRepository
 * @see Batiment
 */
@Service
public class BatimentServiceImpl extends AbstractService<Batiment, BatimentRepository> implements BatimentService {

    /**
     * Initialise le service avec le repository des bâtiments.
     *
     * @param repository repository utilisé pour accéder aux bâtiments
     */
    @Autowired
    public BatimentServiceImpl(BatimentRepository repository) {
        super(repository);
    }

    /**
     * Recherche le nom du bâtiment auquel appartient une salle.
     *
     * <p>Si aucun bâtiment n'est associé à l'identifiant de salle fourni,
     * une {@link ServiceException} est levée.</p>
     *
     * @param salleId identifiant de la salle concernée
     * @return nom du bâtiment associé à la salle
     * @throws ServiceException si aucun bâtiment n'est trouvé pour cette salle
     */
    @Transactional(readOnly = true)
    @Override
    public String findNomBySalleId(Long salleId) {
        // Recherche le bâtiment associé à la salle.
        Optional<Batiment> batiment = repository.findBySallesId(salleId);

        // Signale une erreur si aucun bâtiment n'est associé à cette salle.
        if (batiment.isEmpty()) {
            throw new ServiceException("Batiment Not found with salleId: " + salleId);
        }

        // Retourne le nom du bâtiment trouvé.
        return batiment.get().getNom();
    }

    /**
     * Recherche un bâtiment à partir de son nom.
     *
     * <p>Si aucun bâtiment ne correspond au nom fourni,
     * une {@link ServiceException} est levée.</p>
     *
     * @param nom nom du bâtiment recherché
     * @return bâtiment correspondant au nom fourni
     * @throws ServiceException si aucun bâtiment ne porte ce nom
     */
    @Transactional(readOnly = true)
    @Override
    public Batiment findByNom(String nom) {
        // Recherche le bâtiment par son nom.
        Optional<Batiment> batiment = repository.findBatimentByNom(nom);

        // Interrompt le traitement si aucun bâtiment n'est trouvé.
        if (batiment.isEmpty()) {
            throw new ServiceException("Batiment " + nom + " Not found");
        }

        // Retourne le bâtiment trouvé.
        return batiment.get();
    }
}