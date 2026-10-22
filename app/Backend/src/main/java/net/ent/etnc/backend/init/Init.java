package net.ent.etnc.backend.init;

import net.ent.etnc.backend.models.entities.*;
import net.ent.etnc.backend.models.enums.*;
import net.ent.etnc.backend.services.*;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;


/**
 * Données de test insérées au démarrage si la base est vide (idempotent via {@code userService.count() > 0}).
 *
 * <p>Crée 14 utilisateurs, 3 certifications et 6 sessions dans chaque statut possible :
 * BROUILLON, OUVERTE, FERMEE, EN_CORRECTION, TERMINEE, ANNULEE.
 * Toutes les données sont créées exclusivement via les services pour respecter les règles métier.
 *
 * <p>Les appels qui lisent le {@code SecurityContextHolder} (inscriptions, corrections, validations)
 * sont enveloppés dans {@link #runAs(User, ServiceCall)} qui positionne un contexte temporaire.
 *
 * <p>Compte admin par défaut : {@code admin@certification-center.fr} / {@code Admin@1234}.
 * Tous les autres comptes utilisent le mot de passe {@code Test@1234}.
 */
@Component
public class Init implements CommandLineRunner {

    private final UserService userService;
    private final MaterielService materielService;
    private final UniteCentraleService uniteCentraleService;
    private final EcranService ecranService;
    private final VideoProjecteurService videoProjecteurService;
    private final BatimentService batimentService;
    private final SalleService salleService;
    private final AlerteService alerteService;
    private final StageService stageService;

    public Init(UserService userService, MaterielService materielService, UniteCentraleService uniteCentraleService, EcranService ecranService, VideoProjecteurService videoProjecteurService, BatimentService batimentService, SalleService salleService, AlerteService alerteService, StageService stageService) {
        this.userService = userService;
        this.materielService = materielService;
        this.uniteCentraleService = uniteCentraleService;
        this.ecranService = ecranService;
        this.videoProjecteurService = videoProjecteurService;
        this.batimentService = batimentService;
        this.salleService = salleService;
        this.alerteService = alerteService;
        this.stageService = stageService;
    }

    @Override
    public void run(String... args) throws Exception {
        if(userService.count() <= 0)
            initUsers();
        if(materielService.count() <= 0)
            initMaterial();
        if(batimentService.count() <= 0)
            initBatimentSalle();
        if(alerteService.count() <= 0)
            initAlerte();
        if(stageService.count() == 0)
            initStage();
    }

    private void initUsers() throws ServiceException {
        userService.create(buildUser("0123456789", "admin@example.com", "admin", "Pasquier", "Nicolas", LocalDate.of(1999, 3, 12), Grade.SERGENT, Role.ADMINISTRATEUR));
        userService.create(buildUser("1234567890", "gestionnaire@example.com", "gestionnaire", "Pasquier", "Nicolas", LocalDate.of(1999, 3, 12), Grade.SERGENT, Role.GESTIONNAIRE));
        userService.create(buildUser("2345678901", "formateur@example.com", "formateur", "Pasquier", "Nicolas", LocalDate.of(1999, 3, 12), Grade.SERGENT, Role.FORMATEUR));
        userService.create(buildUser("3456789012", "stagiaire@example.com", "stagiaire", "Pasquier", "Nicolas", LocalDate.of(1999, 3, 12), Grade.SERGENT, Role.STAGIAIRE));
    }

    private User buildUser(String nid, String email, String password, String nom, String prenom, LocalDate dateNaissance, Grade grade, Role role) {
        User u = new User();
        u.setNid(nid);
        u.setEmail(email);
        u.setPassword(password);
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setDateNaissance(dateNaissance);
        u.setGrade(grade);
        u.setRole(role);
        u.setActive(true);
        return u;
    }

    private void initMaterial() throws ServiceException {
        uniteCentraleService.create(buildUC("UC-01", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque1", "modele1", "cpu1", "gpu1", EtatMateriel.EN_ETAT));
        uniteCentraleService.create(buildUC("UC-02", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque1", "modele2", "cpu2", "gpu1", EtatMateriel.EN_MAINTENANCE));
        uniteCentraleService.create(buildUC("UC-03", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque1", "modele2", "cpu2", "gpu1", EtatMateriel.EN_ETAT));
        uniteCentraleService.create(buildUC("UC-04", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque2", "modele11", "cpu1", "gpu2", EtatMateriel.DEPRECIER));
        uniteCentraleService.create(buildUC("UC-05", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque2", "modele11", "cpu1", "gpu2", EtatMateriel.EN_ETAT));

        ecranService.create(buildEcran("ecran-01", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque1", "modele1", 15, EtatMateriel.EN_ETAT));
        ecranService.create(buildEcran("ecran-02", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque1", "modele2", 15, EtatMateriel.EN_ETAT));
        ecranService.create(buildEcran("ecran-03", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque1", "modele2", 15, EtatMateriel.EN_MAINTENANCE));
        ecranService.create(buildEcran("ecran-04", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque1", "modele2", 15, EtatMateriel.EN_ETAT));
        ecranService.create(buildEcran("ecran-05", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque2", "modele11", 18, EtatMateriel.EN_ETAT));
        ecranService.create(buildEcran("ecran-06", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque2", "modele11", 18, EtatMateriel.EN_ETAT));
        ecranService.create(buildEcran("ecran-07", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque2", "modele11", 18, EtatMateriel.DEPRECIER));
        ecranService.create(buildEcran("ecran-08", "nom1", LocalDate.now(), LocalDate.now().plusYears(5), "marque2", "modele11", 18, EtatMateriel.EN_ETAT));

        videoProjecteurService.create(buildVideoProjecteur("VP-01", "nom1", LocalDate.now(), LocalDate.now().plusYears(5),"marque1", "modele1", EtatMateriel.EN_ETAT));
        videoProjecteurService.create(buildVideoProjecteur("VP-02", "nom1", LocalDate.now(), LocalDate.now().plusYears(5),"marque2", "modele12", EtatMateriel.EN_ETAT));
    }

    private UniteCentrale buildUC(String nSerie, String nom, LocalDate dateAjout, LocalDate dateFinGarantie, String marque, String modele, String processeur, String carteGraphique, EtatMateriel etat){
        UniteCentrale uc = new UniteCentrale();

        uc.setNumeroSerie(nSerie);
        uc.setNom(nom);
        uc.setDateAjout(dateAjout);
        uc.setDateFinGarantie(dateFinGarantie);
        uc.setMarque(marque);
        uc.setModele(modele);
        uc.setProcesseur(processeur);
        uc.setCarteGraphique(carteGraphique);
        uc.setEtat(etat);

        return uc;
    }

    private Ecran buildEcran(String nSerie, String nom, LocalDate dateAjout, LocalDate dateFinGarantie, String marque, String modele, Integer taille, EtatMateriel etat){
        Ecran ecran = new Ecran();

        ecran.setNumeroSerie(nSerie);
        ecran.setNom(nom);
        ecran.setDateAjout(dateAjout);
        ecran.setDateFinGarantie(dateFinGarantie);
        ecran.setMarque(marque);
        ecran.setModele(modele);
        ecran.setTaille(taille);
        ecran.setEtat(etat);

        return ecran;
    }

    private VideoProjecteur buildVideoProjecteur(String nSerie, String nom, LocalDate dateAjout, LocalDate dateFinGarantie, String marque, String modele, EtatMateriel etat){
        VideoProjecteur videoProjecteur = new VideoProjecteur();

        videoProjecteur.setNumeroSerie(nSerie);
        videoProjecteur.setNom(nom);
        videoProjecteur.setDateAjout(dateAjout);
        videoProjecteur.setDateFinGarantie(dateFinGarantie);
        videoProjecteur.setMarque(marque);
        videoProjecteur.setModele(modele);
        videoProjecteur.setEtat(etat);

        return videoProjecteur;
    }

    private void initBatimentSalle(){
        batimentService.create(buildBatiment("201", Set.of(
                salleService.create(buildSalle("201", TypeSalle.COURS, 30, EtatSalle.DISPONIBLE, Set.of(
                        materielService.findById(1L).get(),
                        materielService.findById(2L).get(),
                        materielService.findById(6L).get(),
                        materielService.findById(7L).get(),
                        materielService.findById(11L).get(),
                        materielService.findById(14L).get()
                ))),
                salleService.create(buildSalle("001", TypeSalle.STOCKAGE, 60, EtatSalle.DISPONIBLE, Set.of(
                        materielService.findById(5L).get(),
                        materielService.findById(12L).get(),
                        materielService.findById(13L).get()
                )))))
        );
        batimentService.create(buildBatiment("320",Set.of(
                salleService.create(buildSalle("004", TypeSalle.COURS, 30, EtatSalle.DISPONIBLE, Set.of(
                        materielService.findById(3L).get(),
                        materielService.findById(8L).get(),
                        materielService.findById(9L).get()
                ))),
                salleService.create(buildSalle("005", TypeSalle.COURS, 30, EtatSalle.DISPONIBLE, Set.of(
                        materielService.findById(4L).get(),
                        materielService.findById(10L).get(),
                        materielService.findById(15L).get()
                )))))
        );
    }

    private Batiment buildBatiment(String nom, Set<Salle> salles){
        Batiment b = new Batiment();

        b.setNom(nom);
        b.setSalles(salles);

        return b;
    }

    private Salle buildSalle(String nom, TypeSalle typeSalle, Integer nbPlaces, EtatSalle etat, Set<Materiel> materiels){
        Salle s = new Salle();

        s.setNom(nom);
        s.setTypeSalle(typeSalle);
        s.setNbPlaces(nbPlaces);
        s.setEtat(etat);
        s.setMateriels(materiels);

        return s;
    }

    private void initAlerte(){
        User signaleur = userService.findById(4L).get();
        User resolveur = userService.findById(2L).get();

        alerteService.create(buildAlerte(signaleur, LocalDateTime.now(), null, null, "un problême", "ceci est une description", StatutAlerte.EN_ATTENTE));
        alerteService.create(buildAlerte(signaleur, LocalDateTime.now().minusDays(15), resolveur, LocalDateTime.now().minusDays(10), "un deuxième problême", "ceci est une description", StatutAlerte.TRAITEE));
        alerteService.create(buildAlerte(signaleur, LocalDateTime.now().minusDays(20), resolveur, LocalDateTime.now().plusDays(19), "un troisième problême", "ceci est une description", StatutAlerte.ANNULEE));
    }

    private Alerte buildAlerte(User signaleur, LocalDateTime dateSignalement, User resolveur, LocalDateTime dateResolution, String typeProbleme, String description, StatutAlerte statut){
        Alerte alerte = new Alerte();

        alerte.setSignaleur(signaleur);
        alerte.setDateSignalement(dateSignalement);
        alerte.setResolveur(resolveur);
        alerte.setDateResolution(dateResolution);
        alerte.setTypeProbleme(typeProbleme);
        alerte.setDescription(description);
        alerte.setStatut(statut);

        return alerte;
    }

    private void initStage(){
        stageService.create(buildStage("WB47", LocalDate.now().minusMonths(8), LocalDate.now().plusMonths(1), salleService.findById(1L).get(), StatutStage.EN_COURS));
        stageService.create(buildStage("WB48", LocalDate.now().plusDays(8), LocalDate.now().plusMonths(8), null, StatutStage.A_AFFECTER));
    }

    private Stage buildStage(String code, LocalDate dateDebut, LocalDate dateFin, Salle salleCours, StatutStage statut){
        Stage stage = new Stage();

        stage.setCode(code);
        stage.setDateDebut(dateDebut);
        stage.setDateFin(dateFin);
        stage.setSalleCours(salleCours);
        stage.setStatut(statut);

        return stage;
    }

    /**
     * Exécute {@code call} avec {@code user} positionné dans le {@link SecurityContextHolder}.
     * Nettoie le contexte dans tous les cas (finally) pour ne pas polluer les appels suivants.
     */
    private void runAs(User user, ServiceCall call) throws ServiceException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        try {
            call.call();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }



    @FunctionalInterface
    private interface ServiceCall {
        void call() throws ServiceException;
    }
}
