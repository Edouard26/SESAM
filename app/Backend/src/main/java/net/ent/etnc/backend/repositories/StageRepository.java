package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Stage;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StageRepository extends BaseRepository<Stage> {

    Boolean existsByCode(String code);
}