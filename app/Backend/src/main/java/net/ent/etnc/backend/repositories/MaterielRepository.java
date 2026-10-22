package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import net.ent.etnc.backend.repositories.specifications.MaterielSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MaterielRepository extends BaseRepository<Materiel> {

//    @Query("""
//        SELECT m
//        FROM Salle s
//        JOIN s.materiels m
//        WHERE s.id = :salleId
//    """)
    default Page<Materiel> findAllBySalleId(Specification<Materiel> spec, Pageable pageable, Long salleId){
        Specification<Materiel> combinedSpec;

        if(spec == null){
            combinedSpec = Specification.where(
                    MaterielSpecifications.belongsToSalle(salleId));
        }
        else{
            combinedSpec = Specification.where(
                    MaterielSpecifications.belongsToSalle(salleId))
                    .and(spec);
        }

        return findAll(combinedSpec, pageable);
    }



//    @Query("""
//        SELECT m
//        FROM Salle s
//        JOIN s.materiels m
//        WHERE s.id != :salleId
//    """)
    default Page<Materiel> findAllByExcludedSalleId(Specification<Materiel> spec, Pageable pageable, Long salleId){
        Specification<Materiel> combinedSpec;

        if(spec == null){
            combinedSpec = Specification.where(
                    MaterielSpecifications.excludeToSalle(salleId));
        }
        else{
            combinedSpec = Specification.where(
                            MaterielSpecifications.excludeToSalle(salleId))
                    .and(spec);
        }

        return findAll(combinedSpec, pageable);
    }

    Boolean existsByNumeroSerie(String numeroSerie);

    @Query("""
        SELECT COUNT(*) > 0
        FROM Salle s
        JOIN s.materiels m
        WHERE m = :materiel
        AND s = :salle
    """)
    Boolean isInSalle(Materiel materiel, Salle salle);

    @Query("""
        SELECT COUNT(*) > 0
        FROM User u
        JOIN u.materiels m
        WHERE m = :materiel
    """)
    Boolean isAffectedToStagiaire(Materiel materiel);

    @Query("""
        SELECT m
        FROM User u
        JOIN u.materiels m
        WHERE u = :stagiaire
    """)
    Page<Materiel> findAllMaterielAffectToStagiaire(User stagiaire, Pageable pageable);

    List<Materiel> findAllByIdIn(Collection<Long> ids);
}
