package net.ent.etnc.backend.services.impl;

import jakarta.validation.groups.Default;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.enums.EtatSalle;
import net.ent.etnc.backend.models.enums.StatutStage;
import net.ent.etnc.backend.models.validators.groups.UpdateGroup;
import net.ent.etnc.backend.repositories.MaterielRepository;
import net.ent.etnc.backend.repositories.StageRepository;
import net.ent.etnc.backend.repositories.UserRepository;
import net.ent.etnc.backend.services.SalleService;
import net.ent.etnc.backend.services.StageService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Service chargé de la gestion du cycle de vie des stages.
 *
 * @see StageService
 * @see StageRepository
 * @see Stage
 */
@Service
public class StageServiceImpl extends AbstractService<Stage, StageRepository> implements StageService {

    /**
     * Service permettant de gérer les salles associées aux stages.
     */
    public final SalleService salleService;

    /**
     * Repository permettant d'accéder au matériel affecté aux stagiaires.
     */
    private final MaterielRepository materielRepository;

    /**
     * Repository permettant de gérer les utilisateurs et les stagiaires.
     */
    private final UserRepository userRepository;

    /**
     * Initialise le service avec les dépendances nécessaires à la gestion
     * des stages, des salles, du matériel et des utilisateurs.
     *
     * @param repository repository des stages
     * @param salleService service de gestion des salles
     * @param materielRepository repository du matériel
     * @param userRepository repository des utilisateurs
     */
    @Autowired
    public StageServiceImpl(StageRepository repository, SalleService salleService,
                            MaterielRepository materielRepository, UserRepository userRepository) {
        super(repository);
        this.salleService = salleService;
        this.materielRepository = materielRepository;
        this.userRepository = userRepository;
    }

    /**
     * Démarre automatiquement les stages dont la date de début est atteinte
     * et dont le statut est encore {@link StatutStage#AFFECTER}.
     *
     * <p>Cette méthode est exécutée quotidiennement à 1 heure du matin.
     * Les stages concernés passent alors au statut {@link StatutStage#EN_COURS}.</p>
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    @Override
    public void demarrerStage() {
        // Recherche les stages affectés dont la date de début est antérieure à aujourd'hui.
        List<Stage> stagesDemarres = repository.findAllByStatut_AffecterAndDateDebutBefore(LocalDate.now());

        // Passe chaque stage trouvé au statut EN_COURS.
        stagesDemarres.forEach(s -> s.setStatut(StatutStage.EN_COURS));
    }

    /**
     * Clôture automatiquement les stages dont la date de fin est dépassée
     * et qui ne sont pas déjà clôturés.
     *
     * <p>Cette méthode est exécutée quotidiennement à 1 heure du matin.
     * Chaque stage correspondant aux critères est transmis à la méthode
     * {@link #cloturer(Stage)} afin d'effectuer les opérations de clôture.</p>
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    @Override
    public void cloturerStagesTermines() {
        // Recherche les stages terminés dont le statut est différent de CLOTURER.
        List<Stage> stagesTermines = repository.findAllByStatutNotAndDateFinBefore(StatutStage.CLOTURER, LocalDate.now());

        // Applique la procédure de clôture à chaque stage trouvé.
        stagesTermines.forEach(this::cloturer);
    }

    /**
     * Clôture un stage et libère les ressources qui lui sont associées.
     *
     * <p>Cette opération comprend plusieurs étapes :</p>
     * <ul>
     *     <li>Récupération des stagiaires inscrits au stage.</li>
     *     <li>Désaffectation du matériel attribué à ces stagiaires.</li>
     *     <li>Remise à l'état disponible de la salle de cours.</li>
     *     <li>Suppression de l'association entre le stage et sa salle.</li>
     *     <li>Passage du stage au statut {@link StatutStage#CLOTURER}.</li>
     *     <li>Désactivation des comptes des stagiaires concernés.</li>
     * </ul>
     *
     * @param stage stage à clôturer
     */
    @Transactional
    @Override
    public void cloturer(Stage stage) {
        // Récupère l'ensemble des stagiaires inscrits au stage.
        Set<User> stagiaires = stage.getStagiaires();

        // Vérifie que le stage possède des stagiaires avant de rechercher leur matériel.
        if (!stagiaires.isEmpty()) {
            // Récupère tous les matériels affectés aux stagiaires du stage.
            List<Materiel> materiels = materielRepository.findAllByStagiaireIn(stagiaires);

            // Désaffecte chaque matériel en supprimant son association avec le stagiaire.
            materiels.forEach(materiel -> materiel.setStagiaire(null));

            // Enregistre les modifications apportées aux matériels.
            materielRepository.saveAll(materiels);
        }

        // Récupère la salle associée au stage.
        Salle salle = stage.getSalleCours();

        // Vérifie qu'une salle est bien associée au stage.
        if (salle != null) {
            // Rend la salle disponible pour accueillir un nouveau stage.
            salle.setEtat(EtatSalle.DISPONIBLE);

            // Enregistre la modification de l'état de la salle.
            salleService.update(salle);
        }

        // Supprime l'association entre le stage et sa salle de cours.
        stage.setSalleCours(null);

        // Modifie le statut du stage pour indiquer qu'il est clôturé.
        stage.setStatut(StatutStage.CLOTURER);

        // Enregistre les modifications apportées au stage.
        repository.save(stage);

        // Désactive les comptes de tous les stagiaires ayant participé au stage.
        stagiaires.forEach(stagiaire -> stagiaire.setActive(false));

        // Enregistre les modifications des comptes des stagiaires.
        userRepository.saveAll(stagiaires);
    }

    /**
     * Vérifie si un stage existe avec le code fourni.
     *
     * @param code code du stage à rechercher
     * @return {@code true} si un stage possède ce code,
     *         {@code false} sinon
     */
    @Transactional(readOnly = true)
    @Override
    public Boolean existsByCode(String code){
        return repository.existsByCode(code);
    }

    /**
     * Met à jour un stage après vérification de son identifiant
     * et de la disponibilité de la salle pendant toute sa durée.
     *
     * <p>La méthode vérifie d'abord que l'identifiant du stage est renseigné
     * et que le stage existe en base de données. Elle contrôle ensuite
     * la disponibilité de la salle avant d'enregistrer les modifications.</p>
     *
     * <p>En cas d'erreur lors de la mise à jour, une {@link ServiceException}
     * est levée avec l'exception d'origine comme cause.</p>
     *
     * @param stage stage contenant les nouvelles données à enregistrer
     * @return le stage mis à jour
     * @throws ServiceException si l'identifiant est absent, si le stage
     *         n'existe pas ou si une erreur survient pendant la mise à jour
     */
    @Transactional
    @Override
    @Validated({Default.class, UpdateGroup.class})
    public Stage update(Stage stage) throws ServiceException {
        // Vérifie que le stage possède un identifiant pour permettre sa mise à jour.
        if (stage.getId() == null) {
            throw new ServiceException("L'ID du stage ne peut pas être null pour une mise à jour.");
        }

        // Vérifie que le stage à modifier existe en base de données.
        if (!repository.existsById(stage.getId())) {
            throw new ServiceException("Le stage avec l'ID " + stage.getId() + " n'existe pas.");
        }

        try {
            // RG -> La salle destinée à accueillir le stage doit être disponible pendant toute sa durée.
            // Vérifie la disponibilité de la salle sur la période définie par les dates du stage.
            if(salleService.existsSalleLibreDurantPeriode(stage.getSalleCours().getId(), stage.getDateDebut(), stage.getDateFin())){
                // Interrompt la mise à jour si la salle n'est pas disponible.
                throw new ServiceException("Salle " + stage.getSalleCours().getNom() + "non-disponible");
            }

            // Enregistre les modifications du stage si les vérifications sont passées.
            return repository.save(stage);

        } catch (Exception e) {
            // Encapsule toute erreur rencontrée pendant la mise à jour dans une ServiceException.
            throw new ServiceException("Erreur lors de la mise à jour de l'entité", e);
        }
    }
}