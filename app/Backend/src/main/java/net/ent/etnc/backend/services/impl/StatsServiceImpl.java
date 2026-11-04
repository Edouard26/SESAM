package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.dtos.responses.StatsResponse;
import net.ent.etnc.backend.models.enums.EtatMateriel;
import net.ent.etnc.backend.models.enums.EtatSalle;
import net.ent.etnc.backend.models.enums.StatutAlerte;
import net.ent.etnc.backend.models.enums.StatutStage;
import net.ent.etnc.backend.models.enums.TypeMateriel;
import net.ent.etnc.backend.models.enums.TypeSalle;
import net.ent.etnc.backend.repositories.AlerteRepository;
import net.ent.etnc.backend.repositories.EcranRepository;
import net.ent.etnc.backend.repositories.MaterielRepository;
import net.ent.etnc.backend.repositories.SalleRepository;
import net.ent.etnc.backend.repositories.StageRepository;
import net.ent.etnc.backend.repositories.UniteCentraleRepository;
import net.ent.etnc.backend.repositories.VideoProjecteurRepository;
import net.ent.etnc.backend.services.StatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToLongFunction;

/**
 * Service chargé de centraliser et de fournir les statistiques
 * relatives au matériel, aux salles, aux alertes et aux stages.
 *
 * <p>Ce service interroge les différents repositories afin de récupérer
 * les données statistiques et de les regrouper dans un objet
 * {@link StatsResponse} destiné à être utilisé par l'application.</p>
 *
 * @see StatsService
 * @see StatsResponse
 */
@Service
public class StatsServiceImpl implements StatsService {

    /** Repository permettant de récupérer les statistiques du matériel. */
    private final MaterielRepository materielRepository;

    /** Repository permettant de récupérer les statistiques des écrans. */
    private final EcranRepository ecranRepository;

    /** Repository permettant de récupérer les statistiques des unités centrales. */
    private final UniteCentraleRepository uniteCentraleRepository;

    /** Repository permettant de récupérer les statistiques des vidéoprojecteurs. */
    private final VideoProjecteurRepository videoProjecteurRepository;

    /** Repository permettant de récupérer les statistiques des salles. */
    private final SalleRepository salleRepository;

    /** Repository permettant de récupérer les statistiques des alertes. */
    private final AlerteRepository alerteRepository;

    /** Repository permettant de récupérer les statistiques des stages. */
    private final StageRepository stageRepository;

    /**
     * Initialise le service avec les repositories nécessaires
     * à la collecte des différentes statistiques.
     *
     * @param materielRepository repository du matériel
     * @param ecranRepository repository des écrans
     * @param uniteCentraleRepository repository des unités centrales
     * @param videoProjecteurRepository repository des vidéoprojecteurs
     * @param salleRepository repository des salles
     * @param alerteRepository repository des alertes
     * @param stageRepository repository des stages
     */
    public StatsServiceImpl(MaterielRepository materielRepository,
                            EcranRepository ecranRepository,
                            UniteCentraleRepository uniteCentraleRepository,
                            VideoProjecteurRepository videoProjecteurRepository,
                            SalleRepository salleRepository,
                            AlerteRepository alerteRepository,
                            StageRepository stageRepository) {
        this.materielRepository = materielRepository;
        this.ecranRepository = ecranRepository;
        this.uniteCentraleRepository = uniteCentraleRepository;
        this.videoProjecteurRepository = videoProjecteurRepository;
        this.salleRepository = salleRepository;
        this.alerteRepository = alerteRepository;
        this.stageRepository = stageRepository;
    }

    /**
     * Récupère l'ensemble des statistiques de l'application.
     *
     * <p>Cette méthode centralise les données statistiques concernant :</p>
     * <ul>
     *     <li>Le matériel : nombre total, répartition par type, état et garantie.</li>
     *     <li>Les salles : nombre total, répartition par type et état.</li>
     *     <li>Les alertes : nombre total et répartition par statut.</li>
     *     <li>Les stages : nombre total et répartition par statut.</li>
     * </ul>
     *
     * <p>Les différentes statistiques sont regroupées dans un objet
     * {@link StatsResponse} afin de faciliter leur transmission
     * à la couche de présentation.</p>
     *
     * @return un objet contenant l'ensemble des statistiques de l'application
     */
    @Transactional(readOnly = true)
    @Override
    public StatsResponse getStats() {
        // Récupère la date actuelle pour effectuer les calculs liés aux garanties.
        LocalDate today = LocalDate.now();

        // Initialise une map permettant de regrouper le nombre de matériels par type.
        // LinkedHashMap conserve l'ordre d'insertion des différents types.
        Map<String, Long> materielsParType = new LinkedHashMap<>();

        // Compte le nombre d'écrans.
        materielsParType.put(TypeMateriel.ECRAN.name(), ecranRepository.count());

        // Compte le nombre d'unités centrales.
        materielsParType.put(TypeMateriel.UC.name(), uniteCentraleRepository.count());

        // Compte le nombre de vidéoprojecteurs.
        materielsParType.put(TypeMateriel.VIDEO_PROJECTEUR.name(), videoProjecteurRepository.count());

        // Construit et retourne l'objet regroupant toutes les statistiques.
        return new StatsResponse(

                // Statistiques relatives au matériel.
                new StatsResponse.Materiels(
                        // Nombre total de matériels.
                        materielRepository.count(),

                        // Répartition du matériel selon son type.
                        materielsParType,

                        // Répartition du matériel selon son état.
                        countByEnum(EtatMateriel.values(), materielRepository::countByEtat),

                        // Nombre de matériels dont la garantie est déjà expirée.
                        materielRepository.countByDateFinGarantieBefore(today),

                        // Nombre de matériels dont la garantie expire dans les 30 prochains jours.
                        materielRepository.countByDateFinGarantieBetween(today, today.plusDays(30))
                ),

                // Statistiques relatives aux salles.
                new StatsResponse.Salles(
                        // Nombre total de salles.
                        salleRepository.count(),

                        // Nombre de salles de type COURS.
                        salleRepository.countByTypeSalle(TypeSalle.COURS),

                        // Nombre de salles de type STOCKAGE.
                        salleRepository.countByTypeSalle(TypeSalle.STOCKAGE),

                        // Répartition des salles selon leur état.
                        countByEnum(EtatSalle.values(), salleRepository::countByEtat)
                ),

                // Statistiques relatives aux alertes.
                new StatsResponse.Alertes(
                        // Nombre total d'alertes.
                        alerteRepository.count(),

                        // Répartition des alertes selon leur statut.
                        countByEnum(StatutAlerte.values(), alerteRepository::countByStatut)
                ),

                // Statistiques relatives aux stages.
                new StatsResponse.Stages(
                        // Nombre total de stages.
                        stageRepository.count(),

                        // Répartition des stages selon leur statut.
                        countByEnum(StatutStage.values(), stageRepository::countByStatut)
                )
        );
    }

    /**
     * Calcule le nombre d'éléments pour chaque valeur d'une énumération.
     *
     * <p>Cette méthode générique permet de réutiliser le même mécanisme
     * de comptage pour différentes énumérations, comme les états du matériel,
     * les états des salles ou les statuts des alertes et des stages.</p>
     *
     * <p>Chaque valeur de l'énumération est associée à son nombre d'occurrences
     * dans une {@link LinkedHashMap}, en utilisant son nom comme clé.</p>
     *
     * @param values tableau contenant les valeurs de l'énumération
     * @param counter fonction permettant de compter les éléments correspondant
     *                à une valeur de l'énumération
     * @param <E> type de l'énumération utilisée
     * @return une map associant chaque nom de valeur à son nombre d'occurrences
     */
    private static <E extends Enum<E>> Map<String, Long> countByEnum(E[] values, ToLongFunction<E> counter) {
        // Initialise la map qui contiendra les résultats du comptage.
        Map<String, Long> result = new LinkedHashMap<>();

        // Parcourt toutes les valeurs de l'énumération.
        for (E value : values) {
            // Associe le nom de la valeur au nombre d'éléments correspondants.
            result.put(value.name(), counter.applyAsLong(value));
        }

        // Retourne la map contenant les résultats.
        return result;
    }
}
