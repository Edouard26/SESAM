package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.enums.EtatSalle;
import net.ent.etnc.backend.models.enums.TypeSalle;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import net.ent.etnc.backend.repositories.customs.SalleRepositoryCustom;
import net.ent.etnc.backend.repositories.specifications.SalleSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * Repository permettant d'effectuer les opérations de persistance,
 * de recherche et de comptage sur l'entité {@link Salle}.
 *
 * <p>Ce repository étend {@link BaseRepository} et {@link SalleRepositoryCustom}
 * afin de bénéficier des opérations standard de Spring Data JPA et des
 * recherches personnalisées, notamment le tri des salles selon le nombre
 * d'alertes en attente.</p>
 *
 * <p>Il fournit également des méthodes de recherche par bâtiment, par règle,
 * par stagiaire et selon la disponibilité des salles sur une période donnée.</p>
 *
 * @see Salle
 */
@Repository
public interface SalleRepository extends BaseRepository<Salle>, SalleRepositoryCustom {

    /**
     * Recherche les salles associées à une règle donnée.
     *
     * @param regleId identifiant de la règle recherchée
     * @return la liste des salles associées à cette règle
     */
    List<Salle> findAllByRegles_Id(Long regleId);

    /**
     * Compte le nombre de salles correspondant au type indiqué.
     *
     * @param typeSalle type de salle à compter
     * @return le nombre de salles de ce type
     */
    long countByTypeSalle(TypeSalle typeSalle);

    /**
     * Compte le nombre de salles correspondant à l'état indiqué.
     *
     * @param etat état des salles à compter
     * @return le nombre de salles possédant cet état
     */
    long countByEtat(EtatSalle etat);

    /**
     * Recherche la salle de cours associée au stage d'un stagiaire.
     *
     * <p>La requête parcourt les stages et leurs stagiaires afin de retrouver
     * la salle de cours du stage auquel participe l'utilisateur indiqué.</p>
     *
     * @param stagiaire utilisateur représentant le stagiaire recherché
     * @return un {@link Optional} contenant la salle de cours trouvée,
     *         ou un {@link Optional#empty()} si aucune salle ne correspond
     */
    @Query("""
        SELECT stage.salleCours
        FROM Stage stage
        JOIN stage.stagiaires stagiaire
        WHERE stagiaire = :stagiaire
    """)
    Optional<Salle> findByStagiaire(User stagiaire);

    /**
     * Vérifie si une salle n'est occupée par aucun stage pendant la période
     * spécifiée, selon les dates de début et de fin des stages existants.
     *
     * <p>La requête recherche un stage associé à la salle dont la période
     * ne chevauche pas la période demandée.</p>
     *
     * @param salleId identifiant de la salle à vérifier
     * @param dateDebut date de début de la période recherchée
     * @param dateFin date de fin de la période recherchée
     * @return le résultat booléen de la requête
     */
    @Query("""
        SELECT COUNT(*) > 0
        FROM Stage s
        WHERE s.salleCours.id = :salleId
        AND (s.dateDebut > :dateFin OR s.dateFin < :dateDebut)
    """)
    Boolean existsSalleLibreDurantPeriode(Long salleId, LocalDate dateDebut, LocalDate dateFin);

    /**
     * Recherche les salles de cours disponibles pendant une période donnée.
     *
     * <p>La requête sélectionne les salles de type cours qui sont disponibles
     * ou dont un stage associé se situe en dehors de la période demandée.
     * Les résultats sont paginés.</p>
     *
     * @param pageable paramètres de pagination et de tri
     * @param dateDebut date de début de la période recherchée
     * @param dateFin date de fin de la période recherchée
     * @return une page de salles de cours correspondant aux conditions
     *         de disponibilité définies par la requête
     */
    @Query("""
        SELECT salle
        FROM Salle salle
        LEFT JOIN Stage stage ON salle = stage.salleCours
        WHERE salle.typeSalle = TypeSalle.COURS
        AND (salle.etat = EtatSalle.DISPONIBLE
        OR (stage.dateDebut > :dateFin OR stage.dateFin < :dateDebut))
    """)
    Page<Salle> findSallesDisponiblePendantPeriode(Pageable pageable, LocalDate dateDebut, LocalDate dateFin);

    /**
     * Recherche les salles appartenant à un bâtiment donné, en appliquant
     * des critères supplémentaires et un tri par nombre décroissant
     * d'alertes en attente.
     *
     * <p>La spécification liée au bâtiment est combinée avec la spécification
     * fournie. Si cette dernière est {@code null}, seule la restriction
     * au bâtiment est appliquée.</p>
     *
     * @param spec critères supplémentaires de recherche ; peut être {@code null}
     * @param pageable paramètres de pagination et de tri
     * @param batimentId identifiant du bâtiment recherché
     * @return une page de salles correspondant aux critères combinés,
     *         triées selon le nombre d'alertes en attente
     */
    default Page<Salle> findAllByBatimentId(
            Specification<Salle> spec,
            Pageable pageable,
            Long batimentId) {

        // Spécification finale regroupant les critères de recherche.
        Specification<Salle> combinedSpec;

        // Si aucun critère supplémentaire n'est fourni,
        // filtre uniquement les salles du bâtiment indiqué.
        if (spec == null) {
            combinedSpec = Specification.where(
                    SalleSpecification.belongsToBatiment(batimentId));
        } else {
            // Combine l'appartenance au bâtiment avec les critères fournis.
            combinedSpec = Specification.where(
                            SalleSpecification.belongsToBatiment(batimentId))
                    .and(spec);
        }

        // Exécute la recherche paginée avec le tri personnalisé
        // défini dans SalleRepositoryCustom.
        return findAllOrderByAlertesEnAttente(combinedSpec, pageable);
    }
}
