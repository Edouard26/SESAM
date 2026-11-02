package net.ent.etnc.backend.repositories;

import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.repositories.commons.BaseRepository;
import net.ent.etnc.backend.models.enums.StatutAlerte;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlerteRepository extends BaseRepository<Alerte> {
    List<Alerte> findByStatutAndDateResolutionBefore(StatutAlerte statut, LocalDateTime dateResolution);
}
