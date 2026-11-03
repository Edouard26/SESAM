package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Processus;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProcessusRepository extends BaseRepository<Processus> {

    @Query("""
        SELECT p
        FROM UniteCentrale uc
        JOIN uc.processus p
        WHERE uc.id = :uniteCentralId
    """)
    Page<Processus> findAllByUC(Pageable pageable, Long uniteCentralId);

    Optional<Processus> findByNom(String nom);

    @Query("""
        SELECT DISTINCT p.nom
        FROM Salle s
        JOIN s.materiels m
        JOIN s.regles r
        JOIN r.processuss p
        WHERE m.id = :uniteCentralId
    """)
    List<String> findNomsInterditsByUC(Long uniteCentralId);
}
