package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Batiment;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BatimentRepository extends BaseRepository<Batiment> {

    @Query("""
        SELECT b
        FROM Batiment b
        JOIN b.salles s
        WHERE s.id = :salleId
    """)
    Optional<Batiment> findBySallesId(Long salleId);

    Optional<Batiment> findBatimentByNom(String nom);
}