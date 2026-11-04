package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Master;
import net.ent.etnc.backend.services.commons.Service;

/**
 * Interface définissant les opérations métier liées à {@link Master}.
 * <p>
 * Elle étend {@link Service} afin de bénéficier des opérations CRUD génériques
 * applicables aux masters.
 */
public interface MasterService extends Service<Master, Long> {
}
