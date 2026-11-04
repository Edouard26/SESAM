package net.ent.etnc.backend.services;

import net.ent.etnc.backend.models.entities.Processus;
import net.ent.etnc.backend.services.commons.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Interface définissant les opérations métier liées à {@link Processus}.
 * <p>
 * Elle étend {@link Service} afin de bénéficier des opérations CRUD génériques
 * et ajoute des méthodes spécifiques à la gestion des processus associés
 * aux unités centrales.
 */
public interface ProcessusService extends Service<Processus, Long> {

    /**
     * Récupère les processus associés à une unité centrale avec une pagination.
     *
     * @param pageable les paramètres de pagination et de tri
     * @param uniteCentralId l'identifiant de l'unité centrale
     * @return une page contenant les processus associés à l'unité centrale
     */
    @Transactional(readOnly = true)
    Page<Processus> findAllByUC(Pageable pageable, Long uniteCentralId);

    /**
     * Met à jour la liste des processus associés à une unité centrale.
     *
     * @param uniteCentralId l'identifiant de l'unité centrale
     * @param noms la liste des noms des processus à associer
     * @return la liste des processus après mise à jour
     */
    @Transactional
    List<Processus> updateForUC(Long uniteCentralId, List<String> noms);

    /**
     * Met à jour partiellement les processus d'une unité centrale en indiquant
     * ceux qui doivent être démarrés et ceux qui doivent être arrêtés.
     *
     * @param uniteCentralId l'identifiant de l'unité centrale
     * @param started la liste des noms des processus à démarrer
     * @param stopped la liste des noms des processus à arrêter
     */
    @Transactional
    void patchForUC(Long uniteCentralId, List<String> started, List<String> stopped);

    /**
     * Récupère les noms des processus interdits pour une unité centrale.
     *
     * @param uniteCentralId l'identifiant de l'unité centrale
     * @return la liste des noms des processus interdits
     */
    @Transactional(readOnly = true)
    List<String> findNomsInterditsByUC(Long uniteCentralId);
}
