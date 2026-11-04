package net.ent.etnc.backend.repositories.customs;

import net.ent.etnc.backend.models.entities.Salle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Interface définissant les opérations personnalisées du repository
 * de l'entité {@link Salle}.
 *
 * <p>Permet de déclarer des méthodes de recherche spécifiques qui ne sont
 * pas directement fournies par les fonctionnalités standard de Spring Data JPA.</p>
 */
public interface SalleRepositoryCustom {

    /**
     * Recherche les salles correspondant aux critères fournis et les trie
     * par nombre décroissant d'alertes en attente associées à leurs matériels.
     *
     * <p>Les salles ayant le plus d'alertes en attente apparaissent en premier.
     * Les salles sans alerte en attente ont un compteur égal à zéro.</p>
     *
     * <p>La méthode applique les filtres de la {@link Specification}, limite
     * les résultats à la page demandée et calcule le nombre total de salles
     * correspondant aux critères afin de construire les métadonnées de pagination.</p>
     *
     * @param specification critères dynamiques de filtrage des salles ;
     *                      peut être {@code null}
     * @param pageable paramètres de pagination, notamment le numéro de page
     *                 et le nombre de résultats par page
     * @return une page de salles triées par nombre décroissant d'alertes
     *         en attente
     */
    Page<Salle> findAllOrderByAlertesEnAttente(
            Specification<Salle> specification,
            Pageable pageable
    );
}
