package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.models.enums.StatutStage;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StageRepository extends BaseRepository<Stage> {

    Boolean existsByCode(String code);

    long countByStatut(StatutStage statut);

    @Query("""
        SELECT stage
        FROM Stage stage
        WHERE stage.statut = StatutStage.AFFECTER
        AND stage.dateDebut < :date
    """)
    List<Stage> findAllByStatut_AffecterAndDateDebutBefore(LocalDate date);

    @Query("""
        SELECT stage
        FROM Stage stage
        LEFT JOIN FETCH stage.salleCours
        WHERE stage.statut <> :statut
        AND stage.dateFin < :date
    """)
    List<Stage> findAllByStatutNotAndDateFinBefore(StatutStage statut, LocalDate date);
}
