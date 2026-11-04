package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.repositories.MaterielRepository;
import net.ent.etnc.backend.services.MaterielService;
import net.ent.etnc.backend.services.commons.AbstractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Service chargé de la gestion du matériel.
 *
 * @see MaterielService
 * @see MaterielRepository
 * @see Materiel
 */
@Service
public class MaterielServiceImpl extends AbstractService<Materiel, MaterielRepository> implements MaterielService {

    /**
     * Initialise le service avec le repository du matériel.
     *
     * @param repository repository permettant d'accéder aux données du matériel
     */
    public MaterielServiceImpl(MaterielRepository repository) {
        super(repository);
    }

    /**
     * Recherche le matériel associé à une salle en appliquant des critères
     * de filtrage, une pagination et une option d'exclusion des stagiaires.
     *
     * @param spec critères de filtrage du matériel
     * @param pageable paramètres de pagination et de tri
     * @param salleId identifiant de la salle concernée
     * @param excludeStagiaire indique si le matériel associé aux stagiaires doit être exclu
     * @return une page contenant le matériel correspondant aux critères
     */
    @Transactional(readOnly = true)
    @Override
    public Page<Materiel> findAllBySalleId(Specification<Materiel> spec, Pageable pageable, Long salleId, Boolean excludeStagiaire){
        return repository.findAllBySalleId(spec, pageable, salleId, excludeStagiaire);
    }

    /**
     * Recherche le matériel qui n'est pas associé à la salle indiquée,
     * en appliquant les critères de filtrage et la pagination.
     *
     * @param spec critères de filtrage du matériel
     * @param pageable paramètres de pagination et de tri
     * @param salleId identifiant de la salle à exclure
     * @return une page contenant le matériel correspondant aux critères
     */
    @Transactional(readOnly = true)
    @Override
    public Page<Materiel> findAllByExcludedSalleId(Specification<Materiel> spec, Pageable pageable, Long salleId){
        return repository.findAllByExcludedSalleId(spec, pageable, salleId);
    }

    /**
     * Vérifie si un matériel existe avec le numéro de série fourni.
     *
     * @param numeroSerie numéro de série du matériel recherché
     * @return {@code true} si un matériel possède ce numéro de série,
     *         {@code false} sinon
     */
    @Transactional(readOnly = true)
    @Override
    public Boolean existsByNumeroSerie(String numeroSerie){
        return repository.existsByNumeroSerie(numeroSerie);
    }

    /**
     * Vérifie si un matériel peut être affecté à un stagiaire dans une salle.
     *
     * <p>Le matériel doit être présent dans la salle indiquée et ne doit
     * pas déjà être affecté à un stagiaire.</p>
     *
     * @param materiel matériel à vérifier
     * @param salle salle dans laquelle l'affectation est envisagée
     * @return {@code true} si le matériel est présent dans la salle et n'est
     *         pas déjà affecté à un stagiaire, {@code false} sinon
     */
    @Transactional(readOnly = true)
    @Override
    public Boolean isAssignableToStagiaire(Materiel materiel, Salle salle){
        return repository.isInSalle(materiel, salle) && !repository.isAffectedToStagiaire(materiel);
    }

    /**
     * Recherche le matériel affecté à un stagiaire, avec pagination.
     *
     * @param stagiaire stagiaire dont on souhaite récupérer le matériel affecté
     * @param pageable paramètres de pagination et de tri
     * @return une page contenant le matériel affecté au stagiaire
     */
    @Transactional(readOnly = true)
    @Override
    public Page<Materiel> findAllMaterielAffectToStagiaire(User stagiaire, Pageable pageable){
        return repository.findAllMaterielAffectToStagiaire(stagiaire, pageable);
    }

    /**
     * Recherche tous les matériels dont les identifiants figurent
     * dans la collection fournie.
     *
     * @param ids collection des identifiants des matériels recherchés
     * @return la liste des matériels correspondant aux identifiants fournis
     */
    @Transactional(readOnly = true)
    @Override
    public List<Materiel> findAllByIdIn(Collection<Long> ids){
        return repository.findAllByIdIn(ids);
    }
}
