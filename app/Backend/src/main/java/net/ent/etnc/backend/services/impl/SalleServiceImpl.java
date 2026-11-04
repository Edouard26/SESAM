package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.entities.Regle;
import net.ent.etnc.backend.repositories.SalleRepository;
import net.ent.etnc.backend.repositories.RegleRepository;
import net.ent.etnc.backend.services.SalleService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.HashSet;

/**
 * Service chargé de la gestion des salles.
 *
 * @see SalleService
 * @see SalleRepository
 * @see RegleRepository
 * @see Salle
 */
@Service
public class SalleServiceImpl extends AbstractService<Salle, SalleRepository> implements SalleService {

    /**
     * Repository permettant d'accéder aux règles applicables aux salles.
     */
    private final RegleRepository regleRepository;

    /**
     * Initialise le service avec les repositories nécessaires
     * à la gestion des salles et des règles.
     *
     * @param repository repository des salles
     * @param regleRepository repository des règles
     */
    @Autowired
    public SalleServiceImpl(SalleRepository repository, RegleRepository regleRepository) {
        super(repository);
        this.regleRepository = regleRepository;
    }

    /**
     * Recherche les salles en les triant selon le nombre d'alertes en attente.
     *
     * <p>La recherche prend en compte les critères de filtrage et les paramètres
     * de pagination fournis.</p>
     *
     * @param specification critères de filtrage des salles
     * @param pageable paramètres de pagination et de tri
     * @return une page de salles triées selon leurs alertes en attente
     */
    @Transactional(readOnly = true)
    @Override
    public Page<Salle> findAllOrderByAlertesEnAttente(Specification<Salle> specification, Pageable pageable){
        return repository.findAllOrderByAlertesEnAttente(specification, pageable);
    }

    /**
     * Recherche la salle associée à un stagiaire.
     *
     * <p>Si aucune salle n'est associée au stagiaire fourni,
     * une {@link ServiceException} est levée.</p>
     *
     * @param stagiaire stagiaire dont on souhaite retrouver la salle
     * @return la salle associée au stagiaire
     * @throws ServiceException si aucune salle n'est trouvée
     */
    @Transactional(readOnly = true)
    @Override
    public Salle findByStagiaire(User stagiaire){
        // Recherche la salle associée au stagiaire.
        Optional<Salle> optionalSalle = repository.findByStagiaire(stagiaire);

        // Vérifie qu'une salle a bien été trouvée.
        if(optionalSalle.isEmpty()){
            throw new ServiceException("Salle not found");
        }

        // Retourne la salle trouvée.
        return optionalSalle.get();
    }

    /**
     * Recherche les salles disponibles pendant la période d'un stage.
     *
     * <p>Les dates de début et de fin du stage sont utilisées pour déterminer
     * les salles pouvant accueillir celui-ci.</p>
     *
     * @param pageable paramètres de pagination et de tri
     * @param stage stage dont on souhaite connaître les salles disponibles
     * @return une page contenant les salles disponibles pendant la période du stage
     */
    @Transactional(readOnly = true)
    @Override
    public Page<Salle> findSallesDisponiblePourStage(Pageable pageable, Stage stage){
        return repository.findSallesDisponiblePendantPeriode(pageable, stage.getDateDebut(), stage.getDateFin());
    }

    /**
     * Recherche les salles appartenant à un bâtiment donné.
     *
     * <p>La recherche prend en compte les critères de filtrage
     * et les paramètres de pagination.</p>
     *
     * @param spec critères de filtrage des salles
     * @param pageable paramètres de pagination et de tri
     * @param batimentId identifiant du bâtiment concerné
     * @return une page contenant les salles du bâtiment correspondant aux critères
     */
    @Transactional(readOnly = true)
    @Override
    public Page<Salle> findAllByBatimentId(Specification<Salle> spec, Pageable pageable, Long batimentId){
        return repository.findAllByBatimentId(spec, pageable, batimentId);
    }

    /**
     * Met à jour les règles associées à une salle.
     *
     * <p>Recherche d'abord la salle concernée, puis récupère les règles
     * correspondant aux identifiants fournis. Vérifie ensuite que toutes
     * les règles demandées existent avant de remplacer les règles
     * actuellement associées à la salle.</p>
     *
     * <p>Si la salle ou l'une des règles est introuvable,
     * une {@link ServiceException} est levée.</p>
     *
     * @param salleId identifiant de la salle à modifier
     * @param reglesIds liste des identifiants des règles à associer à la salle
     * @return la salle enregistrée avec ses nouvelles règles
     * @throws ServiceException si la salle ou une ou plusieurs règles sont introuvables
     */
    @Transactional
    @Override
    public Salle updateRegles(Long salleId, List<Long> reglesIds) {
        // Recherche la salle à modifier ou lève une exception si elle n'existe pas.
        Salle salle = repository.findById(salleId)
                .orElseThrow(() -> new ServiceException("Salle introuvable."));

        // Récupère les règles correspondant aux identifiants fournis.
        // Si la liste est null, utilise une liste vide.
        List<Regle> regles = regleRepository.findAllById(reglesIds == null ? List.of() : reglesIds);

        // Vérifie que toutes les règles demandées ont été retrouvées.
        // Les doublons éventuels dans les identifiants ne sont comptés qu'une seule fois.
        if (regles.size() != (reglesIds == null ? 0 : new HashSet<>(reglesIds).size())) {
            throw new ServiceException("Une ou plusieurs règles sont introuvables.");
        }

        // Remplace les règles actuellement associées à la salle.
        salle.setRegles(new HashSet<>(regles));

        // Enregistre la salle avec ses nouvelles associations et retourne le résultat.
        return repository.save(salle);
    }

    /**
     * Vérifie si une salle est libre pendant une période donnée.
     *
     * @param salleId identifiant de la salle à vérifier
     * @param dateDebut date de début de la période
     * @param dateFin date de fin de la période
     * @return {@code true} si la salle est libre pendant toute la période,
     *         {@code false} sinon
     */
    @Transactional(readOnly = true)
    @Override
    public Boolean existsSalleLibreDurantPeriode(Long salleId, LocalDate dateDebut, LocalDate dateFin){
        return repository.existsSalleLibreDurantPeriode(salleId, dateDebut, dateFin);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Salle> findByMaterielId(Long materielId){
        return repository.findFirstByMateriels_Id(materielId);
    }
}
