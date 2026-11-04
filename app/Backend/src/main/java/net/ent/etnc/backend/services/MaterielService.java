package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Interface définissant les opérations métier liées à {@link Materiel}.
 * <p>
 * Elle étend {@link Service} afin de bénéficier des opérations CRUD génériques
 * et ajoute des méthodes spécifiques à la recherche, à l'affectation et à la
 * vérification des matériels.
 */
public interface MaterielService extends Service<Materiel, Long> {

    /**
     * Récupère les matériels associés à une salle en appliquant des critères
     * de recherche et une pagination.
     *
     * @param spec les critères de filtrage des matériels
     * @param pageable les paramètres de pagination et de tri
     * @param salleId l'identifiant de la salle
     * @param excludeStagiaire indique si les matériels affectés aux stagiaires doivent être exclus
     * @return une page contenant les matériels correspondant aux critères
     */
    @Transactional(readOnly = true)
    Page<Materiel> findAllBySalleId(Specification<Materiel> spec, Pageable pageable, Long salleId, Boolean excludeStagiaire);

    /**
     * Récupère les matériels qui ne sont pas associés à la salle indiquée,
     * en appliquant des critères de recherche et une pagination.
     *
     * @param spec les critères de filtrage des matériels
     * @param pageable les paramètres de pagination et de tri
     * @param salleId l'identifiant de la salle à exclure
     * @return une page contenant les matériels correspondants
     */
    @Transactional(readOnly = true)
    Page<Materiel> findAllByExcludedSalleId(Specification<Materiel> spec, Pageable pageable, Long salleId);

    /**
     * Vérifie si un matériel existe avec le numéro de série fourni.
     *
     * @param numeroSerie le numéro de série à rechercher
     * @return {@code true} si un matériel possède ce numéro de série, {@code false} sinon
     */
    @Transactional(readOnly = true)
    Boolean existsByNumeroSerie(String numeroSerie);

    /**
     * Vérifie si un matériel peut être affecté à un stagiaire dans une salle donnée.
     *
     * @param materiel le matériel à vérifier
     * @param salle la salle concernée par l'affectation
     * @return {@code true} si le matériel peut être affecté au stagiaire, {@code false} sinon
     */
    @Transactional(readOnly = true)
    Boolean isAssignableToStagiaire(Materiel materiel, Salle salle);

    /**
     * Récupère les matériels affectés à un stagiaire avec une pagination.
     *
     * @param stagiaire le stagiaire concerné
     * @param pageable les paramètres de pagination et de tri
     * @return une page contenant les matériels affectés au stagiaire
     */
    @Transactional(readOnly = true)
    Page<Materiel> findAllMaterielAffectToStagiaire(User stagiaire, Pageable pageable);

    /**
     * Récupère les matériels dont les identifiants figurent dans la collection fournie.
     *
     * @param ids la collection des identifiants des matériels à rechercher
     * @return la liste des matériels correspondants
     */
    @Transactional(readOnly = true)
    List<Materiel> findAllByIdIn(Collection<Long> ids);
}
