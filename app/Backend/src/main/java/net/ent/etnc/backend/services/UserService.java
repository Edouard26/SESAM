package net.ent.etnc.backend.services;

import net.ent.etnc.backend.dtos.requests.MaterielAssigmentsStagiaireRequest;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface définissant les opérations métier liées à {@link User}.
 * <p>
 * Elle étend {@link Service} afin de bénéficier des opérations CRUD génériques
 * et ajoute des méthodes spécifiques à la recherche des utilisateurs et à
 * l'affectation de matériels aux stagiaires.
 */
public interface UserService extends Service<User, Long> {

    /**
     * Vérifie si un utilisateur existe avec le numéro d'identification national fourni.
     *
     * @param nid le numéro d'identification national à rechercher
     * @return {@code true} si un utilisateur possède ce numéro, {@code false} sinon
     */
    @Transactional(readOnly = true)
    Boolean existsByNid(String nid);

    /**
     * Recherche un utilisateur à partir de son numéro d'identification national.
     *
     * @param nid le numéro d'identification national de l'utilisateur
     * @return l'utilisateur correspondant au numéro fourni
     */
    @Transactional(readOnly = true)
    User findByNid(String nid);

    /**
     * Affecte des matériels à un stagiaire à partir des informations
     * contenues dans la requête fournie.
     *
     * @param request l'objet contenant les informations nécessaires à l'affectation
     * @return l'utilisateur stagiaire après l'affectation des matériels
     */
    @Transactional
    User AssigmentsMaterielToStagiaire(MaterielAssigmentsStagiaireRequest request);
}
