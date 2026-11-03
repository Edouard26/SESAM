package net.ent.etnc.backend.repositories.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import org.springframework.data.jpa.domain.Specification;

/**
 * Classe utilitaire permettant de définir des spécifications JPA pour filtrer les matériels selon différents critères.
 */
public class MaterielSpecifications {

    /**
     * Crée une spécification permettant de récupérer uniquement les matériels appartenant à une salle donnée.
     *
     * <p>La recherche utilise une sous-requête qui parcourt la relation
     * entre {@link Salle} et ses matériels, puis sélectionne les identifiants
     * des matériels associés à la salle spécifiée.</p>
     *
     * @param salleId identifiant de la salle à laquelle les matériels doivent appartenir
     * @return une spécification JPA permettant de filtrer les matériels par salle
     */
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

    /**
     * Crée une spécification permettant de récupérer uniquement les matériels qui ne sont affectés à aucun stagiaire.
     *
     * <p>La sous-requête sélectionne les identifiants des matériels dont
     * l'attribut {@code stagiaire} est nul. La requête principale conserve
     * uniquement les matériels correspondant à ce critère.</p>
     *
     * @return une spécification JPA permettant de filtrer les matériels
     *         non affectés à un stagiaire
     */
    public static Specification<Materiel> notAssignToStagiaire(){
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);

            Root<Materiel> materiel = subquery.from(Materiel.class);

            subquery.select(materiel.get("id"))
                    .where(cb.equal(materiel.get("stagiaire"), null));

            return root.get("id").in(subquery);
        };
    }

    /**
     * Crée une spécification permettant d'exclure les matériels appartenant à une salle donnée.
     *
     * <p>La sous-requête parcourt les salles et leurs matériels afin de sélectionner
     * les identifiants des matériels associés à une salle dont l'identifiant
     * est différent de celui fourni.</p>
     *
     * @param salleId identifiant de la salle à exclure
     * @return une spécification JPA permettant de filtrer les matériels
     *         associés à des salles différentes de celle spécifiée
     */
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
