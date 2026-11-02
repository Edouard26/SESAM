package net.ent.etnc.backend.repositories.customs;

import net.ent.etnc.backend.models.entities.Salle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface SalleRepositoryCustom {
    Page<Salle> findAllOrderByAlertesEnAttente(
            Specification<Salle> specification,
            Pageable pageable
    );
}
