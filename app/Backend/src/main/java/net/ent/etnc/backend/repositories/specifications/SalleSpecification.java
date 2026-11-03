package net.ent.etnc.backend.repositories.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import net.ent.etnc.backend.models.entities.Batiment;
import net.ent.etnc.backend.models.entities.Salle;
import org.springframework.data.jpa.domain.Specification;

/**
 * Classe utilitaire permettant de définir des spécifications JPA pour filtrer les salles selon différents critères.
 */
public class SalleSpecification {

    /**
     * Crée une spécification permettant de récupérer uniquement les salles appartenant à un bâtiment donné.
     *
     * <p>La recherche utilise une sous-requête qui parcourt la relation
     * entre {@link Batiment} et ses salles, puis sélectionne les identifiants
     * des salles associées au bâtiment spécifié.</p>
     *
     * @param batimentId identifiant du bâtiment auquel les salles doivent appartenir
     * @return une spécification JPA permettant de filtrer les salles par bâtiment
     */
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
