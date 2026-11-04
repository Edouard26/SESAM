package net.ent.etnc.backend.repositories.customs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.enums.StatutAlerte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implémentation personnalisée du repository de l'entité {@link Salle}.
 *
 * <p>Cette classe permet d'effectuer des recherches avancées sur les salles
 * à l'aide de l'API Criteria de JPA, notamment pour trier les salles selon
 * le nombre d'alertes en attente associées aux matériels qu'elles contiennent.</p>
 *
 * <p>Elle prend également en charge le filtrage dynamique via une
 * {@link Specification} et la pagination via {@link Pageable}.</p>
 *
 */
@Repository
@RequiredArgsConstructor
public class SalleRepositoryCustomImpl
        implements SalleRepositoryCustom {

    private final EntityManager entityManager;

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
    @Override
    public Page<Salle> findAllOrderByAlertesEnAttente(
            Specification<Salle> specification,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Requête principale : toutes les salles
        CriteriaQuery<Salle> query = cb.createQuery(Salle.class);
        Root<Salle> salle = query.from(Salle.class);

        // Sous-requête : compter les alertes en attente liées aux matériels
        // de la salle courante
        Subquery<Long> subquery = query.subquery(Long.class);

        Root<Alerte> alerte = subquery.from(Alerte.class);
        Join<Alerte, Materiel> materielAlerte =
                alerte.join("materiels", JoinType.INNER);

        // Corrélation avec les matériels de la salle courante
        Subquery<Long> materielsSalle = subquery.subquery(Long.class);
        Root<Salle> salleSousRequete = materielsSalle.from(Salle.class);
        Join<Salle, Materiel> materielSalle =
                salleSousRequete.join("materiels", JoinType.INNER);

        materielsSalle.select(materielSalle.get("id"));
        materielsSalle.where(
                cb.equal(salleSousRequete.get("id"), salle.get("id")),
                cb.equal(
                        materielSalle.get("id"),
                        materielAlerte.get("id")
                )
        );

        subquery.select(cb.countDistinct(alerte.get("id")));
        subquery.where(
                cb.equal(alerte.get("statut"), StatutAlerte.EN_ATTENTE),
                cb.exists(materielsSalle)
        );

        // Filtres dynamiques
        if (specification != null) {
            Predicate predicate = specification.toPredicate(salle, query, cb);

            if (predicate != null) {
                query.where(predicate);
            }
        }

        // Tri décroissant : les salles sans alerte auront un compteur à zéro
        query.orderBy(cb.desc(subquery));

        List<Salle> salles = entityManager.createQuery(query)
                .setFirstResult(Math.toIntExact(pageable.getOffset()))
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Comptage des salles pour la pagination
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Salle> countRoot = countQuery.from(Salle.class);

        countQuery.select(cb.count(countRoot));

        if (specification != null) {
            Predicate countPredicate =
                    specification.toPredicate(countRoot, countQuery, cb);

            if (countPredicate != null) {
                countQuery.where(countPredicate);
            }
        }

        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(salles, pageable, total);
    }
}
