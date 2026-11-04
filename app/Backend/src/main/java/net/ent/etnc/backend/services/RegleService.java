package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Regle;
import net.ent.etnc.backend.services.commons.Service;

/**
 * Interface définissant les opérations métier liées à {@link Regle}.
 * <p>
 * Elle étend {@link Service} afin de bénéficier des opérations CRUD génériques
 * applicables aux règles.
 */
public interface RegleService extends Service<Regle, Long> {
}
