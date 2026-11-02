package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Processus;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface ProcessusRepository extends BaseRepository<Processus> {

    @Query("""
        SELECT p
        FROM UniteCentrale uc
        JOIN uc.processus p
        WHERE uc.id = :uniteCentralId
    """)
    Page<Processus> findAllByUC(Pageable pageable, Long uniteCentralId);
}
