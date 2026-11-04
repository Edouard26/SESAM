package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.VideoProjecteur;
import net.ent.etnc.backend.services.commons.Service;

/**
 * Interface définissant les opérations métier liées à {@link VideoProjecteur}.
 * <p>
 * Elle étend {@link Service} afin de bénéficier des opérations CRUD génériques
 * applicables aux vidéoprojecteurs.
 */
public interface VideoProjecteurService extends Service<VideoProjecteur, Long> {
}