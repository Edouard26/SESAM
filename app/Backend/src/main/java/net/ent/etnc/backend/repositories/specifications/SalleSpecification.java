package net.ent.etnc.backend.repositories.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import net.ent.etnc.backend.models.entities.Batiment;
import net.ent.etnc.backend.models.entities.Salle;
import org.springframework.data.jpa.domain.Specification;

/**
 * TODO
 */
public class SalleSpecification {
    public static Specification<Salle> belongsToBatiment(Long batimentId) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);

            Root<Batiment> batiment = subquery.from(Batiment.class);
            Join<Batiment, Salle> salle = batiment.join("salles");

            subquery.select(salle.get("id"))
                    .where(cb.equal(batiment.get("id"), batimentId));

            return root.get("id").in(subquery);
        };
    }
}
