package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.models.enums.StatutStage;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository permettant d'effectuer les opérations de persistance,
 * de recherche et de comptage sur l'entité {@link Stage}.
 *
 * <p>Fournit des méthodes pour vérifier l'existence d'un stage,
 * compter les stages selon leur statut et rechercher les stages
 * en fonction de leur statut et de leurs dates.</p>
 *
 * @see Stage
 * @see StatutStage
 */
@Repository
public interface StageRepository extends BaseRepository<Stage> {

    /**
     * Vérifie si un stage existe avec le code indiqué.
     *
     * @param code code du stage à rechercher
     * @return {@code true} si un stage possède ce code,
     *         {@code false} sinon
     */
    Boolean existsByCode(String code);

    /**
     * Compte le nombre de stages possédant le statut indiqué.
     *
     * @param statut statut des stages à compter
     * @return le nombre de stages correspondant au statut
     */
    long countByStatut(StatutStage statut);

    /**
     * Recherche les stages affectés dont la date de début est antérieure
     * à la date spécifiée.
     *
     * <p>La requête sélectionne les stages dont le statut est
     * {@link StatutStage#AFFECTER} et dont la date de début précède
     * la date fournie.</p>
     *
     * @param date date limite de début des stages
     * @return la liste des stages affectés ayant commencé avant cette date
     */
    @Query("""
        SELECT stage
        FROM Stage stage
        WHERE stage.statut = StatutStage.AFFECTER
        AND stage.dateDebut < :date
    """)
    List<Stage> findAllByStatut_AffecterAndDateDebutBefore(LocalDate date);

    /**
     * Recherche les stages dont le statut est différent du statut indiqué
     * et dont la date de fin est antérieure à la date spécifiée.
     *
     * <p>La salle de cours associée est chargée explicitement grâce à
     * {@code LEFT JOIN FETCH}, afin de pouvoir accéder à cette relation
     * sans déclencher de chargement différé supplémentaire.</p>
     *
     * @param statut statut à exclure des résultats
     * @param date date limite de fin des stages
     * @return la liste des stages correspondant aux critères
     */
    @Query("""
        SELECT stage
        FROM Stage stage
        LEFT JOIN FETCH stage.salleCours
        WHERE stage.statut <> :statut
        AND stage.dateFin < :date
    """)
    List<Stage> findAllByStatutNotAndDateFinBefore(StatutStage statut, LocalDate date);
}
