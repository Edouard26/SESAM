package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Salle;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalleRepository extends BaseRepository<Salle> {

    @Query("""
        SELECT stage.salleCours
        FROM Stage stage
        JOIN stage.stagiaires stagiaire
        WHERE stagiaire = :stagiaire
    """)
    Optional<Salle> findByStagiaire(User stagiaire);
}