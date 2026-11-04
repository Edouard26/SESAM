package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.models.entities.User;

import java.time.LocalDate;
import java.util.List;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface définissant les opérations métier liées à {@link Salle}.
 * <p>
 * Elle étend {@link Service} afin de bénéficier des opérations CRUD génériques
 * et ajoute des méthodes spécifiques à la recherche, à la disponibilité et à
 * la mise à jour des salles.
 */
public interface SalleService extends Service<Salle, Long> {

    /**
     * Récupère les salles triées selon le nombre d'alertes en attente,
     * en appliquant les critères de recherche et la pagination.
     *
     * @param specification les critères de filtrage des salles
     * @param pageable les paramètres de pagination et de tri
     * @return une page de salles triées selon leurs alertes en attente
     */
    @Transactional(readOnly = true)
    Page<Salle> findAllOrderByAlertesEnAttente(Specification<Salle> specification, Pageable pageable);

    /**
     * Recherche la salle associée à un stagiaire.
     *
     * @param stagiaire le stagiaire concerné
     * @return la salle associée au stagiaire
     */
    @Transactional(readOnly = true)
    Salle findByStagiaire(User stagiaire);

    /**
     * Récupère les salles disponibles pour accueillir un stage.
     *
     * @param pageable les paramètres de pagination et de tri
     * @param stage le stage concerné par la recherche
     * @return une page contenant les salles disponibles pour ce stage
     */
    @Transactional(readOnly = true)
    Page<Salle> findSallesDisponiblePourStage(Pageable pageable, Stage stage);

    /**
     * Récupère les salles appartenant à un bâtiment donné,
     * en appliquant des critères de recherche et une pagination.
     *
     * @param spec les critères de filtrage des salles
     * @param pageable les paramètres de pagination et de tri
     * @param batimentId l'identifiant du bâtiment
     * @return une page contenant les salles du bâtiment
     */
    @Transactional(readOnly = true)
    Page<Salle> findAllByBatimentId(Specification<Salle> spec, Pageable pageable, Long batimentId);

    /**
     * Met à jour les règles associées à une salle.
     *
     * @param salleId l'identifiant de la salle
     * @param reglesIds la liste des identifiants des règles à associer
     * @return la salle après la mise à jour de ses règles
     */
    @Transactional
    Salle updateRegles(Long salleId, List<Long> reglesIds);

    /**
     * Vérifie si une salle est libre durant une période donnée.
     *
     * @param salleId l'identifiant de la salle
     * @param dateDebut la date de début de la période
     * @param dateFin la date de fin de la période
     * @return {@code true} si la salle est libre durant toute la période, {@code false} sinon
     */
    @Transactional(readOnly = true)
    Boolean existsSalleLibreDurantPeriode(Long salleId, LocalDate dateDebut, LocalDate dateFin);
}
