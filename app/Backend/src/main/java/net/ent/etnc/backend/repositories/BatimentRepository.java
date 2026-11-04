package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Batiment;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository permettant d'effectuer les opérations de persistance
 * et de recherche sur l'entité {@link Batiment}.
 *
 * <p>Fournit notamment des méthodes de recherche d'un bâtiment
 * à partir de l'identifiant d'une salle associée ou du nom du bâtiment.</p>
 *
 * @see Batiment
 */
@Repository
public interface BatimentRepository extends BaseRepository<Batiment> {

    /**
     * Recherche le bâtiment auquel appartient une salle donnée.
     *
     * <p>La requête JPQL effectue une jointure entre le bâtiment et ses salles,
     * puis filtre les résultats à partir de l'identifiant de la salle.</p>
     *
     * @param salleId identifiant de la salle recherchée
     * @return un {@link Optional} contenant le bâtiment associé à la salle,
     *         ou un {@link Optional#empty()} si aucun bâtiment ne correspond
     */
    @Query("""
        SELECT b
        FROM Batiment b
        JOIN b.salles s
        WHERE s.id = :salleId
    """)
    Optional<Batiment> findBySallesId(Long salleId);

    /**
     * Recherche un bâtiment à partir de son nom.
     *
     * <p>La méthode dérivée s'appuie sur le nom du champ {@code nom}
     * de l'entité {@link Batiment} pour construire automatiquement la requête.</p>
     *
     * @param nom nom du bâtiment recherché
     * @return un {@link Optional} contenant le bâtiment correspondant,
     *         ou un {@link Optional#empty()} si aucun bâtiment ne correspond
     */
    Optional<Batiment> findBatimentByNom(String nom);
}