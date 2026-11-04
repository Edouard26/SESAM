package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import net.ent.etnc.backend.models.enums.StatutAlerte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository permettant d'effectuer les opérations de persistance
 * et de recherche sur les alertes.
 *
 * <p>Fournit des méthodes de recherche par statut, par utilisateur
 * et par salle, ainsi que des opérations de comptage des alertes.</p>
 *
 * <p>Les requêtes utilisent à la fois les méthodes dérivées de Spring Data JPA,
 * JPQL et SQL natif.</p>
 *
 * @see Alerte
 */
@Repository
public interface AlerteRepository extends BaseRepository<Alerte> {

    /**
     * Recherche les alertes correspondant à un statut donné et dont
     * la date de résolution est antérieure à la date spécifiée.
     *
     * @param statut         statut des alertes recherchées
     * @param dateResolution date limite de résolution
     * @return la liste des alertes correspondant aux critères
     */
    List<Alerte> findByStatutAndDateResolutionBefore(StatutAlerte statut, LocalDateTime dateResolution);

    /**
     * Recherche les alertes signalées par un utilisateur donné.
     *
     * <p>La recherche est effectuée à partir de l'identifiant du signaleur.
     * Les résultats sont retournés sous forme de page afin de permettre
     * leur consultation par lots.</p>
     *
     * @param pageable paramètres de pagination et de tri
     * @param userId   identifiant de l'utilisateur ayant signalé les alertes
     * @return une page contenant les alertes signalées par cet utilisateur
     */
    @Query("""
        SELECT a
        FROM Alerte a
        WHERE a.signaleur.id = :userId
    """)
    Page<Alerte> findAllByUserId(Pageable pageable, Long userId);

    /**
     * Compte le nombre d'alertes en attente associées aux matériels
     * d'une salle donnée.
     *
     * <p>La requête SQL relie les salles, les matériels, la table de jointure
     * des alertes et les alertes elles-mêmes. Le comptage distinct évite
     * de compter plusieurs fois une même alerte lorsqu'elle est associée
     * à plusieurs matériels de la salle.</p>
     *
     * @param salleId identifiant de la salle concernée
     * @return le nombre d'alertes distinctes en attente dans cette salle
     */
    @Query(value = """
        SELECT COUNT (DISTINCT a)
        FROM salles s
        JOIN materiels m ON s.id = m.salle_id
        JOIN alerte_materiels am ON m.id = am.materiel_id
        JOIN alertes a ON a.id = am.alerte_id
        WHERE s.id = :salleId
        AND a.statut = 'EN_ATTENTE'
    """, nativeQuery = true)
    Integer countAlerteInSalle(Long salleId);

    /**
     * Recherche les alertes en attente associées aux matériels d'une salle.
     *
     * <p>Les résultats sont paginés et les alertes sont sélectionnées
     * distinctement afin d'éviter les doublons lorsqu'une même alerte
     * concerne plusieurs matériels de la salle.</p>
     *
     * @param pageable paramètres de pagination et de tri
     * @param salleId  identifiant de la salle concernée
     * @return une page contenant les alertes en attente associées à cette salle
     */
    @Query(value = """
        SELECT DISTINCT a.id, date_resolution, date_signalement, description, statut, type_probleme, resolveur_id, signaleur_id
        FROM salles s
        JOIN materiels m ON s.id = m.salle_id
        JOIN alerte_materiels am ON m.id = am.materiel_id
        JOIN alertes a ON a.id = am.alerte_id
        WHERE s.id = :salleId
        AND a.statut = 'EN_ATTENTE'
    """, nativeQuery = true)
    Page<Alerte> findAllBySalleId(Pageable pageable, Long salleId);

    /**
     * Compte le nombre total d'alertes possédant un statut donné.
     *
     * @param statut statut des alertes à compter
     * @return le nombre d'alertes correspondant au statut
     */
    long countByStatut(StatutAlerte statut);
}