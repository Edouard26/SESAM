package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AlerteRepository extends BaseRepository<Alerte> {

    @Query(value = """
        SELECT COUNT (DISTINCT a)
        FROM salles s
        JOIN materiels m ON s.id = m.salle_id
        JOIN alerte_materiels am ON m.id = am.materiel_id
        JOIN alertes a ON a.id = am.alerte_id
        WHERE s.id = :salleId
        AND a.statut = 'EN_ATTENTE'
    """, nativeQuery = true)
    Integer countAlerteInSalle(Long salleId);

    @Query(value = """
        SELECT DISTINCT a.id, date_resolution, date_signalement, description, statut, type_probleme, resolveur_id, signaleur_id
        FROM salles s
        JOIN materiels m ON s.id = m.salle_id
        JOIN alerte_materiels am ON m.id = am.materiel_id
        JOIN alertes a ON a.id = am.alerte_id
        WHERE s.id = :salleId
        AND a.statut = 'EN_ATTENTE'
    """, nativeQuery = true)
    Page<Alerte> findAllBySalleId(Pageable pageable, Long salleId);
}