package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Batiment;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.transaction.annotation.Transactional;

public interface BatimentService extends Service<Batiment, Long> {

    @Transactional(readOnly = true)
    String findNomBySalleId(Long salleId);

    @Transactional(readOnly = true)
    Batiment findByNom(String nom);
}