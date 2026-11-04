package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.UniteCentrale;
import net.ent.etnc.backend.services.commons.Service;

/**
 * Interface définissant les opérations métier liées à {@link UniteCentrale}.
 * <p>
 * Elle étend {@link Service} afin de bénéficier des opérations CRUD génériques
 * applicables aux unités centrales.
 */
public interface UniteCentraleService extends Service<UniteCentrale, Long> {
}