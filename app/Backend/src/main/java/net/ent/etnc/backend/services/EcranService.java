package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Ecran;
import net.ent.etnc.backend.services.commons.Service;

/**
 * Interface définissant les opérations métier liées à {@link Ecran}.
 * <p>
 * Elle étend {@link Service} afin de bénéficier des opérations CRUD génériques
 * applicables aux écrans.
 */
public interface EcranService extends Service<Ecran, Long> {

}