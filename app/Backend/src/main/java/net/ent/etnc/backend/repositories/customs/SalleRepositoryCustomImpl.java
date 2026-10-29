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

@Repository
@RequiredArgsConstructor
public class SalleRepositoryCustomImpl
        implements SalleRepositoryCustom {

    private final EntityManager entityManager;

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
