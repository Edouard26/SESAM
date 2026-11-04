package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Batiment;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface définissant les opérations métier liées à {@link Batiment}.
 * <p>
 * Elle étend {@link Service} afin de bénéficier des opérations CRUD génériques
 * et ajoute des méthodes spécifiques à la recherche des bâtiments.
 */
public interface BatimentService extends Service<Batiment, Long> {

    /**
     * Recherche le nom du bâtiment auquel appartient une salle.
     *
     * @param salleId l'identifiant de la salle
     * @return le nom du bâtiment associé à la salle
     */
    @Transactional(readOnly = true)
    String findNomBySalleId(Long salleId);

    /**
     * Recherche un bâtiment à partir de son nom.
     *
     * @param nom le nom du bâtiment à rechercher
     * @return le bâtiment correspondant au nom fourni
     */
    @Transactional(readOnly = true)
    Batiment findByNom(String nom);
}