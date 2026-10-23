package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface StageRepository extends BaseRepository<Stage> {

    Boolean existsByCode(String code);

    // TODO Peut être la mettre dans SalleRepository
    @Query("""
        SELECT COUNT(*) > 0
        FROM Stage s
        WHERE s.salleCours.id = :salleId
        AND(s.dateDebut > :dateFin OR s.dateFin < :dateDebut)
    """)
    Boolean existsSalleLibreDurantPeriode(Long salleId, LocalDate dateDebut, LocalDate dateFin);
}