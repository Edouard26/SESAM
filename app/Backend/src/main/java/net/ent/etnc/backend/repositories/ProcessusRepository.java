package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Processus;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository permettant d'effectuer les opérations de persistance
 * et de recherche sur l'entité {@link Processus}.
 *
 * <p>Fournit des méthodes de recherche des processus associés à une unité
 * centrale, de recherche par nom et de récupération des noms de processus
 * interdits pour un matériel donné.</p>
 *
 * @see Processus
 */
@Repository
public interface ProcessusRepository extends BaseRepository<Processus> {

    /**
     * Recherche les processus associés à une unité centrale donnée.
     *
     * <p>La requête effectue une jointure entre les unités centrales
     * et leurs processus associés. Les résultats sont paginés.</p>
     *
     * @param pageable paramètres de pagination et de tri
     * @param uniteCentralId identifiant de l'unité centrale concernée
     * @return une page des processus associés à cette unité centrale
     */
    @Query("""
        SELECT p
        FROM UniteCentrale uc
        JOIN uc.processus p
        WHERE uc.id = :uniteCentralId
    """)
    Page<Processus> findAllByUC(Pageable pageable, Long uniteCentralId);

    /**
     * Recherche un processus à partir de son nom.
     *
     * @param nom nom du processus recherché
     * @return un {@link Optional} contenant le processus trouvé,
     *         ou un {@link Optional#empty()} si aucun processus ne correspond
     */
    Optional<Processus> findByNom(String nom);

    /**
     * Récupère les noms distincts des processus interdits pour un matériel
     * associé à une unité centrale donnée.
     *
     * <p>La requête parcourt les salles, leurs matériels, les règles associées
     * aux salles et les processus liés à ces règles. Elle retourne uniquement
     * les noms distincts des processus correspondant au matériel identifié.</p>
     *
     * @param uniteCentralId identifiant du matériel utilisé pour filtrer
     *                       les résultats
     * @return la liste des noms distincts des processus interdits
     */
    @Query("""
        SELECT DISTINCT p.nom
        FROM Salle s
        JOIN s.materiels m
        JOIN s.regles r
        JOIN r.processuss p
        WHERE m.id = :uniteCentralId
    """)
    List<String> findNomsInterditsByUC(Long uniteCentralId);
}
