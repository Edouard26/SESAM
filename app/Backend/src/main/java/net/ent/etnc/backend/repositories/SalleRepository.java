package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import net.ent.etnc.backend.repositories.customs.SalleRepositoryCustom;
import net.ent.etnc.backend.repositories.specifications.SalleSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SalleRepository extends BaseRepository<Salle>, SalleRepositoryCustom {

    @Query("""
        SELECT stage.salleCours
        FROM Stage stage
        JOIN stage.stagiaires stagiaire
        WHERE stagiaire = :stagiaire
    """)
    Optional<Salle> findByStagiaire(User stagiaire);

    // TODO rajouter la période de stage mais surement devoir faire un ManyToOne dans le stage
    @Query("""
        SELECT salle
        FROM Salle salle
        LEFT JOIN Stage stage ON salle = stage.salleCours
        WHERE salle.typeSalle = TypeSalle.COURS
        AND salle.etat = EtatSalle.DISPONIBLE
    """)
    Page<Salle> findSallesDisponiblePendantPeriode(Pageable pageable, LocalDate dateDebut, LocalDate dateFin);

    default Page<Salle> findAllByBatimentId(Specification<Salle> spec, Pageable pageable, Long batimentId){
        Specification<Salle> combinedSpec;

        if(spec == null){
            combinedSpec = Specification.where(
                    SalleSpecification.belongsToBatiment(batimentId));
        }
        else{
            combinedSpec = Specification.where(
                    SalleSpecification.belongsToBatiment(batimentId))
                    .and(spec);
        }

        return findAllOrderByAlertesEnAttente(combinedSpec, pageable);
    }
}