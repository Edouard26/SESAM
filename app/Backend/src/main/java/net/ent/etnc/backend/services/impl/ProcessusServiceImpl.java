package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Processus;
import net.ent.etnc.backend.models.entities.UniteCentrale;
import net.ent.etnc.backend.repositories.ProcessusRepository;
import net.ent.etnc.backend.repositories.UniteCentraleRepository;
import net.ent.etnc.backend.services.ProcessusService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Service chargé de la gestion des processus associés aux unités centrales.
 *
 * @see ProcessusService
 * @see ProcessusRepository
 * @see UniteCentraleRepository
 */
@Service
public class ProcessusServiceImpl extends AbstractService<Processus, ProcessusRepository> implements ProcessusService {

    /**
     * Repository permettant d'accéder aux unités centrales.
     */
    private final UniteCentraleRepository uniteCentraleRepository;

    /**
     * Initialise le service avec les repositories nécessaires à la gestion
     * des processus et des unités centrales.
     *
     * @param repository repository des processus
     * @param uniteCentraleRepository repository des unités centrales
     */
    public ProcessusServiceImpl(ProcessusRepository repository, UniteCentraleRepository uniteCentraleRepository) {
        super(repository);
        this.uniteCentraleRepository = uniteCentraleRepository;
    }

    /**
     * Recherche les processus associés à une unité centrale avec pagination.
     *
     * @param pageable paramètres de pagination et de tri
     * @param uniteCentralId identifiant de l'unité centrale
     * @return une page contenant les processus associés à cette unité centrale
     */
    @Transactional(readOnly = true)
    @Override
    public Page<Processus> findAllByUC(Pageable pageable, Long uniteCentralId){
        return repository.findAllByUC(pageable, uniteCentralId);
    }

    /**
     * Remplace la liste des processus associés à une unité centrale.
     *
     * <p>Recherche d'abord l'unité centrale. Les noms fournis sont dédupliqués
     * tout en conservant leur ordre d'apparition. Chaque processus existant
     * est réutilisé ; si aucun processus ne correspond au nom, un nouveau
     * processus est créé et enregistré.</p>
     *
     * <p>La liste des processus de l'unité centrale est ensuite remplacée
     * par la liste obtenue, puis l'unité centrale est enregistrée.</p>
     *
     * @param uniteCentralId identifiant de l'unité centrale à mettre à jour
     * @param noms noms des processus à associer à l'unité centrale
     * @return la liste des processus associés après la mise à jour
     * @throws ServiceException si l'unité centrale n'existe pas
     */
    @Transactional
    @Override
    public List<Processus> updateForUC(Long uniteCentralId, List<String> noms) {
        // Recherche l'unité centrale ou lève une exception si elle n'existe pas.
        UniteCentrale uc = uniteCentraleRepository.findById(uniteCentralId)
                .orElseThrow(() -> new ServiceException("L'unité centrale avec l'ID " + uniteCentralId + " n'existe pas."));

        // Prépare la liste des processus à associer à l'unité centrale.
        List<Processus> processus = new ArrayList<>();

        // Supprime les doublons tout en conservant l'ordre des noms fournis.
        for (String nom : new LinkedHashSet<>(noms)) {
            // Réutilise le processus existant ou crée et enregistre un nouveau processus.
            processus.add(repository.findByNom(nom).orElseGet(() -> {
                Processus nouveau = new Processus();
                nouveau.setNom(nom);
                return repository.save(nouveau);
            }));
        }

        // Remplace les processus associés à l'unité centrale par la nouvelle liste.
        uc.setProcessus(processus);

        // Enregistre les modifications de l'unité centrale.
        uniteCentraleRepository.save(uc);

        return processus;
    }

    /**
     * Met à jour partiellement les processus associés à une unité centrale.
     *
     * <p>Retire les processus dont le nom figure dans la liste {@code stopped},
     * puis ajoute ceux dont le nom figure dans la liste {@code started},
     * à condition qu'ils ne soient pas déjà associés à l'unité centrale.</p>
     *
     * <p>Les processus qui n'existent pas encore en base sont créés et
     * enregistrés. L'unité centrale est ensuite sauvegardée.</p>
     *
     * @param uniteCentralId identifiant de l'unité centrale à modifier
     * @param started noms des processus à ajouter
     * @param stopped noms des processus à retirer
     * @throws ServiceException si l'unité centrale n'existe pas
     */
    @Transactional
    @Override
    public void patchForUC(Long uniteCentralId, List<String> started, List<String> stopped) {
        // Recherche l'unité centrale ou lève une exception si elle n'existe pas.
        UniteCentrale uc = uniteCentraleRepository.findById(uniteCentralId)
                .orElseThrow(() -> new ServiceException("L'unité centrale avec l'ID " + uniteCentralId + " n'existe pas."));

        // Retire de l'unité centrale les processus dont le nom figure dans stopped.
        uc.getProcessus().removeIf(p -> stopped.contains(p.getNom()));

        // Parcourt les noms des processus à ajouter.
        for (String nom : started) {
            // Ignore le processus s'il est déjà associé à l'unité centrale.
            if (uc.getProcessus().stream().anyMatch(p -> p.getNom().equals(nom))) continue;

            // Réutilise le processus existant ou crée et enregistre un nouveau processus.
            uc.getProcessus().add(repository.findByNom(nom).orElseGet(() -> {
                Processus nouveau = new Processus();
                nouveau.setNom(nom);
                return repository.save(nouveau);
            }));
        }

        // Enregistre les modifications de l'unité centrale.
        uniteCentraleRepository.save(uc);
    }

    /**
     * Récupère les noms des processus interdits pour une unité centrale.
     *
     * @param uniteCentralId identifiant de l'unité centrale
     * @return la liste des noms des processus interdits pour cette unité centrale
     */
    @Transactional(readOnly = true)
    @Override
    public List<String> findNomsInterditsByUC(Long uniteCentralId) {
        return repository.findNomsInterditsByUC(uniteCentralId);
    }
}
