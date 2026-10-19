package net.ent.etnc.backend.repositories.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import org.springframework.data.jpa.domain.Specification;

/**
 * TODO
 */
public class MaterielSpecifications {

    public static Specification<Materiel> belongsToSalle(Long salleId) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);

            Root<Salle> salle = subquery.from(Salle.class);
            Join<Salle, Materiel> materiel = salle.join("materiels");

            subquery.select(materiel.get("id"))
                    .where(cb.equal(salle.get("id"), salleId));

            return root.get("id").in(subquery);
        };
    }

    public static Specification<Materiel> excludeToSalle(Long salleId){
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);

            Root<Salle> salle = subquery.from(Salle.class);
            Join<Salle, Materiel> materiel = salle.join("materiels");

            subquery.select(materiel.get("id"))
                    .where(cb.notEqual(salle.get("id"), salleId));

            return root.get("id").in(subquery);
        };
    }
}
