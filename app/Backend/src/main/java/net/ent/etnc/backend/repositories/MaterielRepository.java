package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.enums.EtatMateriel;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import net.ent.etnc.backend.repositories.specifications.MaterielSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Repository permettant d'effectuer les opérations de persistance,
 * de recherche et de comptage sur l'entité {@link Materiel}.
 *
 * <p>Ce repository étend {@link BaseRepository} et fournit des méthodes
 * personnalisées pour rechercher les matériels selon leur salle,
 * leur affectation à un stagiaire, leur état et leur garantie.</p>
 *
 * <p>Les recherches peuvent utiliser des spécifications dynamiques
 * ({@link Specification}) et la pagination ({@link Pageable}).</p>
 *
 * @see Materiel
 * @see Salle
 * @see User
 * @see EtatMateriel
 */
@Repository
public interface MaterielRepository extends BaseRepository<Materiel> {

    /**
     * Recherche les matériels d'une salle en appliquant des critères
     * dynamiques et, si demandé, en excluant ceux affectés à un stagiaire.
     *
     * <p>La spécification de recherche est combinée avec le critère
     * d'appartenance à la salle. Lorsque {@code excludeStagiaire} vaut
     * {@code true}, les matériels affectés à un stagiaire sont exclus.</p>
     *
     * @param spec critères supplémentaires de recherche ; peut être {@code null}
     * @param pageable paramètres de pagination et de tri
     * @param salleId identifiant de la salle concernée
     * @param excludeStagiaire indique si les matériels affectés à un stagiaire
     *                         doivent être exclus
     * @return une page de matériels correspondant aux critères
     */
    default Page<Materiel> findAllBySalleId(Specification<Materiel> spec, Pageable pageable, Long salleId, Boolean excludeStagiaire) {

        // Spécification finale combinant les critères de recherche.
        Specification<Materiel> combinedSpec;

        // Si demandé, exclut les matériels affectés à un stagiaire.
        if (excludeStagiaire) {

            // Aucun critère supplémentaire n'a été fourni.
            if (spec == null) {
                combinedSpec = Specification.where(
                        MaterielSpecifications.belongsToSalle(salleId)
                                .and(MaterielSpecifications.notAssignToStagiaire()));
            } else {
                // Combine l'appartenance à la salle, les critères supplémentaires
                // et l'absence d'affectation à un stagiaire.
                combinedSpec = Specification.where(
                                MaterielSpecifications.belongsToSalle(salleId)
                        )
                        .and(spec)
                        .and(MaterielSpecifications.notAssignToStagiaire());
            }
        } else {

            // Recherche uniquement les matériels de la salle si aucun
            // critère supplémentaire n'a été fourni.
            if (spec == null) {
                combinedSpec = Specification.where(
                        MaterielSpecifications.belongsToSalle(salleId));
            } else {
                // Combine l'appartenance à la salle avec les critères fournis.
                combinedSpec = Specification.where(
                                MaterielSpecifications.belongsToSalle(salleId))
                        .and(spec);
            }
        }

        // Exécute la recherche avec les critères combinés et la pagination.
        return findAll(combinedSpec, pageable);
    }

    /**
     * Recherche les matériels qui ne sont pas associés à la salle indiquée,
     * puis applique les éventuels critères supplémentaires.
     *
     * @param spec critères supplémentaires de recherche ; peut être {@code null}
     * @param pageable paramètres de pagination et de tri
     * @param salleId identifiant de la salle à exclure
     * @return une page de matériels correspondant aux critères
     */
    default Page<Materiel> findAllByExcludedSalleId(Specification<Materiel> spec, Pageable pageable, Long salleId) {

        // Construit la spécification de base excluant la salle indiquée.
        Specification<Materiel> combinedSpec;

        if (spec == null) {
            combinedSpec = Specification.where(
                    MaterielSpecifications.excludeToSalle(salleId));
        } else {
            // Combine l'exclusion de la salle avec les critères supplémentaires.
            combinedSpec = Specification.where(
                            MaterielSpecifications.excludeToSalle(salleId))
                    .and(spec);
        }

        // Exécute la recherche paginée.
        return findAll(combinedSpec, pageable);
    }

    /**
     * Vérifie si un matériel existe avec le numéro de série indiqué.
     *
     * @param numeroSerie numéro de série à rechercher
     * @return {@code true} si un matériel possède ce numéro de série,
     *         {@code false} sinon
     */
    Boolean existsByNumeroSerie(String numeroSerie);

    /**
     * Vérifie si un matériel est associé à une salle donnée.
     *
     * @param materiel matériel à vérifier
     * @param salle salle dans laquelle rechercher le matériel
     * @return {@code true} si le matériel appartient à la salle,
     *         {@code false} sinon
     */
    @Query("""
        SELECT COUNT(*) > 0
        FROM Salle s
        JOIN s.materiels m
        WHERE m = :materiel
        AND s = :salle
    """)
    Boolean isInSalle(Materiel materiel, Salle salle);

    /**
     * Vérifie si un matériel est affecté à un stagiaire.
     *
     * @param materiel matériel dont on souhaite vérifier l'affectation
     * @return {@code true} si le matériel est affecté à un stagiaire,
     *         {@code false} sinon
     */
    @Query("""
        SELECT COUNT(*) > 0
        FROM Materiel m
        JOIN m.stagiaire s
        WHERE m = :materiel
    """)
    Boolean isAffectedToStagiaire(Materiel materiel);

    /**
     * Recherche les matériels affectés à un stagiaire donné.
     *
     * @param stagiaire utilisateur représentant le stagiaire recherché
     * @param pageable paramètres de pagination et de tri
     * @return une page des matériels affectés à ce stagiaire
     */
    @Query("""
        SELECT m
        FROM Materiel m
        JOIN m.stagiaire s
        WHERE s = :stagiaire
    """)
    Page<Materiel> findAllMaterielAffectToStagiaire(User stagiaire, Pageable pageable);

    /**
     * Recherche les matériels affectés à un autre stagiaire que celui indiqué.
     *
     * <p>Cette requête ne retourne pas les matériels dont le stagiaire
     * associé est nul, car elle utilise une jointure interne.</p>
     *
     * @param stagiaire stagiaire à exclure
     * @param pageable paramètres de pagination et de tri
     * @return une page des matériels affectés à un autre stagiaire
     */
    @Query("""
        SELECT m
        FROM Materiel m
        JOIN m.stagiaire s
        WHERE s <> :stagiaire
    """)
    Page<Materiel> findAllMaterielNotAffectToStagiaire(User stagiaire, Pageable pageable);

    /**
     * Recherche tous les matériels affectés à au moins un utilisateur
     * appartenant à la collection fournie.
     *
     * @param stagiaires collection des utilisateurs à rechercher
     * @return la liste des matériels associés à ces utilisateurs
     */
    List<Materiel> findAllByStagiaireIn(Collection<User> stagiaires);

    /**
     * Recherche les matériels dont l'identifiant appartient à la collection
     * fournie.
     *
     * @param ids collection des identifiants des matériels recherchés
     * @return la liste des matériels correspondants
     */
    List<Materiel> findAllByIdIn(Collection<Long> ids);

    /**
     * Compte le nombre de matériels possédant l'état indiqué.
     *
     * @param etat état des matériels à compter
     * @return le nombre de matériels correspondant à cet état
     */
    long countByEtat(EtatMateriel etat);

    /**
     * Compte les matériels dont la date de fin de garantie est antérieure
     * à la date spécifiée.
     *
     * @param date date limite de fin de garantie
     * @return le nombre de matériels dont la garantie se termine avant cette date
     */
    long countByDateFinGarantieBefore(LocalDate date);

    /**
     * Compte les matériels dont la date de fin de garantie se situe
     * dans l'intervalle indiqué, bornes incluses.
     *
     * @param debut date de début de l'intervalle
     * @param fin date de fin de l'intervalle
     * @return le nombre de matériels dont la fin de garantie se situe
     *         entre les deux dates
     */
    long countByDateFinGarantieBetween(LocalDate debut, LocalDate fin);
}
