package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface définissant les opérations métier liées à {@link Alerte}.
 * <p>
 * Elle étend {@link Service} afin de bénéficier des opérations CRUD génériques
 * et ajoute des méthodes spécifiques à la recherche et au comptage des alertes.
 */
public interface AlerteService extends Service<Alerte, Long> {

    /**
     * Recherche une alerte par son identifiant en tenant compte de l'utilisateur
     * qui effectue la demande.
     *
     * @param user l'utilisateur effectuant la demande
     * @param alerteId l'identifiant de l'alerte à rechercher
     * @return l'alerte correspondante
     */
    @Transactional(readOnly = true)
    Alerte findById(User user, Long alerteId);

    /**
     * Récupère les alertes associées à un utilisateur avec une pagination.
     *
     * @param pageable les paramètres de pagination et de tri
     * @param userId l'identifiant de l'utilisateur
     * @return une page contenant les alertes de l'utilisateur
     */
    @Transactional(readOnly = true)
    Page<Alerte> findAllByUserId(Pageable pageable, Long userId);

    /**
     * Compte le nombre d'alertes associées à une salle.
     *
     * @param salleId l'identifiant de la salle
     * @return le nombre d'alertes associées à cette salle
     */
    @Transactional(readOnly = true)
    Integer countAlerteInSalle(Long salleId);

    /**
     * Récupère les alertes associées à une salle avec une pagination.
     *
     * @param pageable les paramètres de pagination et de tri
     * @param salleId l'identifiant de la salle
     * @return une page contenant les alertes de la salle
     */
    @Transactional(readOnly = true)
    Page<Alerte> findAllBySalleId(Pageable pageable, Long salleId);
}