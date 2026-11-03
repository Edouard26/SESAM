package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.enums.EtatSalle;
import net.ent.etnc.backend.models.enums.TypeSalle;
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
import java.util.List;

@Repository
public interface SalleRepository extends BaseRepository<Salle>, SalleRepositoryCustom {

    List<Salle> findAllByRegles_Id(Long regleId);

    long countByTypeSalle(TypeSalle typeSalle);

    long countByEtat(EtatSalle etat);

    @Query("""
        SELECT stage.salleCours
        FROM Stage stage
        JOIN stage.stagiaires stagiaire
        WHERE stagiaire = :stagiaire
    """)
    Optional<Salle> findByStagiaire(User stagiaire);

    @Query("""
        SELECT COUNT(*) > 0
        FROM Stage s
        WHERE s.salleCours.id = :salleId
        AND(s.dateDebut > :dateFin OR s.dateFin < :dateDebut)
    """)
    Boolean existsSalleLibreDurantPeriode(Long salleId, LocalDate dateDebut, LocalDate dateFin);

    @Query("""
        SELECT salle
        FROM Salle salle
        LEFT JOIN Stage stage ON salle = stage.salleCours
        WHERE salle.typeSalle = TypeSalle.COURS
        AND (salle.etat = EtatSalle.DISPONIBLE
        OR (stage.dateDebut > :dateFin OR stage.dateFin < :dateDebut))
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
