package net.ent.etnc.backend.services.impl;

import net.ent.etnc.backend.models.entities.Alerte;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.models.enums.Role;
import net.ent.etnc.backend.models.enums.StatutAlerte;
import net.ent.etnc.backend.repositories.AlerteRepository;
import net.ent.etnc.backend.services.AlerteService;
import net.ent.etnc.backend.services.commons.AbstractService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service de gestion des alertes.
 *
 * @see AlerteService
 * @see AlerteRepository
 * @see Alerte
 */
@Service
public class AlerteServiceImpl extends AbstractService<Alerte, AlerteRepository> implements AlerteService {

    /**
     * Initialise le service avec le repository utilisé pour accéder
     * aux données des alertes.
     *
     * @param repository repository de persistance des alertes
     */
    @Autowired
    public AlerteServiceImpl(AlerteRepository repository) {
        super(repository);
    }

    /**
     * Supprime les alertes traitées dont la date de résolution remonte
     * à plus d'un an.
     *
     * <p>Cette méthode est exécutée automatiquement chaque jour à minuit
     * grâce à l'annotation {@link Scheduled}. Elle recherche les alertes
     * ayant le statut {@link StatutAlerte#TRAITEE} et une date de résolution
     * antérieure à la date limite, puis les supprime.</p>
     *
     * <p>L'ensemble de l'opération est exécuté dans une transaction.</p>
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void supprimerAlertesTraiteesAnciennes() {
        // Détermine la date limite : un an avant la date et l'heure actuelles.
        LocalDateTime dateLimite = LocalDateTime.now().minusYears(1);

        // Récupère les alertes traitées dont la résolution est antérieure à cette date.
        List<Alerte> alertesAnciennes = repository.findByStatutAndDateResolutionBefore(
                        StatutAlerte.TRAITEE,
                        dateLimite
                );

        // Supprime les alertes récupérées dans la base de données.
        repository.deleteAll(alertesAnciennes);
    }

    /**
     * Recherche une alerte par son identifiant et vérifie les droits
     * de consultation de l'utilisateur.
     *
     * <p>Selon la règle métier appliquée ici, un stagiaire ne peut consulter
     * que les alertes qu'il a lui-même signalées. Si l'alerte n'existe pas
     * ou si l'utilisateur ne satisfait pas cette règle, une
     * {@link ServiceException} est levée.</p>
     *
     * @param user utilisateur qui demande à consulter l'alerte
     * @param alerteId identifiant de l'alerte recherchée
     * @return l'alerte correspondante si elle existe et si l'accès est autorisé
     * @throws ServiceException si l'alerte n'existe pas ou si l'accès est refusé
     */
    @Transactional(readOnly = true)
    @Override
    public Alerte findById(User user, Long alerteId) {
        // Recherche l'alerte à partir de son identifiant.
        Optional<Alerte> optionalAlerte = repository.findById(alerteId);

        // Interrompt le traitement si aucune alerte ne correspond à l'identifiant.
        if (optionalAlerte.isEmpty()) {
            throw new ServiceException("Alerte not found");
        }

        // Règle métier : un stagiaire ne peut consulter que les alertes dont il est le signaleur.
        if (user.getRole() != Role.STAGIAIRE || !optionalAlerte.get().getSignaleur().equals(user)) {
            throw new ServiceException("l'user n'est pas le signaleur de l'alerte");
        }

        // Retourne l'alerte après validation des conditions d'accès.
        return optionalAlerte.get();
    }

    /**
     * Recherche les alertes associées à un utilisateur, avec pagination.
     *
     * @param pageable paramètres de pagination et de tri
     * @param userId identifiant de l'utilisateur concerné
     * @return une page contenant les alertes associées à cet utilisateur
     */
    @Transactional(readOnly = true)
    @Override
    public Page<Alerte> findAllByUserId(Pageable pageable, Long userId) {
        // Délègue au repository la recherche paginée des alertes de l'utilisateur.
        return repository.findAllByUserId(pageable, userId);
    }

    /**
     * Compte les alertes associées à une salle.
     *
     * @param salleId identifiant de la salle concernée
     * @return le nombre d'alertes associées à cette salle
     */
    @Transactional(readOnly = true)
    @Override
    public Integer countAlerteInSalle(Long salleId) {
        // Récupère auprès du repository le nombre d'alertes de la salle.
        return repository.countAlerteInSalle(salleId);
    }

    /**
     * Recherche les alertes associées à une salle, avec pagination.
     *
     * @param pageable paramètres de pagination et de tri
     * @param salleId identifiant de la salle concernée
     * @return une page contenant les alertes associées à cette salle
     */
    @Transactional(readOnly = true)
    @Override
    public Page<Alerte> findAllBySalleId(Pageable pageable, Long salleId) {
        // Délègue au repository la recherche paginée des alertes de la salle.
        return repository.findAllBySalleId(pageable, salleId);
    }
}