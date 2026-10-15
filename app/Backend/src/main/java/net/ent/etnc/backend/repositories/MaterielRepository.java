package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Materiel;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterielRepository extends BaseRepository<Materiel> {

    @Query("""
        SELECT m
        FROM Salle s
        JOIN s.materiels m
        WHERE s.id = :salleId
    """)
    Page<Materiel> findAllBySalleId(Specification<Materiel> spec, Pageable pageable, Long salleId);

    @Query("""
        SELECT m
        FROM Salle s
        JOIN s.materiels m
        WHERE s.id != :salleId
    """)
    Page<Materiel> findAllByExcludedSalleId(Specification<Materiel> spec, Pageable pageable, Long salleId);
}
