package net.ent.etnc.backend.init;

import net.ent.etnc.backend.models.entities.*;
import net.ent.etnc.backend.models.enums.*;
import net.ent.etnc.backend.services.*;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Comparator;

/**
 * Jeu de test SESAM : 500 matériels, 5 bâtiments, 100 salles,
 * 100 utilisateurs dont 70 stagiaires.
 *
 * Relations CODASYL respectées : Batiment.salles, Salle.materiels,
 * Stage.salleCours, Stage.stagiaires et Materiel.stagiaire.
 * Groupes de 20, 20, 20 et 10 stagiaires sur une base initialement vide.
 * 60 stagiaires reçoivent chacun une UC et un écran EN_ETAT dans leur salle.
 *
 * Les créations restent conditionnées à des catégories vides.
 * Les relations manquantes sont aussi complétées sur l'ancien jeu de test.
 * Aucun utilisateur n'est retiré de son stage, aucun matériel déjà attribué
 * n'est repris et les salles des autres stages ne sont pas prélevées.
 *
 * Comptes principaux : admin, gestionnaire, formateur, stagiaire,
 * suivis de @intradef.gouv.fr ; mots de passe identiques aux identifiants.
 * Autres comptes : Test@1234. Noms commerciaux réels, séries fictives.
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

    private User gestionnaireCree;
    private User stagiaireCree;

    public Init(UserService userService, MaterielService materielService,
                UniteCentraleService uniteCentraleService, EcranService ecranService,
                VideoProjecteurService videoProjecteurService,
                BatimentService batimentService, SalleService salleService,
                AlerteService alerteService, StageService stageService) {
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
    @Transactional
    public void run(String... args) throws Exception {
        // Une seule transaction : les associations JPA chargées restent gérées
        // et toutes les modifications sont annulées si l'initialisation échoue.
        if (userService.count() == 0) {
            initUsers();
        }
        if (materielService.count() == 0) {
            initMaterial();
        }
        if (batimentService.count() == 0) {
            initBatimentSalle();
        }

        List<User> utilisateurs = new ArrayList<>(userService.findAll());
        List<Salle> salles = new ArrayList<>(salleService.findAll());
        List<Materiel> materiels = new ArrayList<>(materielService.findAll());
        salles.sort(Comparator.comparing(Salle::getNom));
        materiels.sort(Comparator.comparing(Materiel::getNumeroSerie));

        rattacherSallesAuxBatiments(salles);
        List<Stage> groupes = initStagesEtGroupes(utilisateurs, salles);
        affecterMateriels(groupes, salles, materiels);

        if (alerteService.count() == 0) {
            gestionnaireCree = utilisateurs.stream()
                    .filter(u -> u.getRole() == Role.GESTIONNAIRE)
                    .findFirst().orElseThrow(() -> new IllegalStateException("Aucun gestionnaire."));
            stagiaireCree = utilisateurs.stream()
                    .filter(u -> u.getRole() == Role.STAGIAIRE)
                    .findFirst().orElseThrow(() -> new IllegalStateException("Aucun stagiaire."));
            initAlerte();
        }
    }

    private void initUsers() throws ServiceException {
        userService.create(buildUser("0123456789", "admin@intradef.gouv.fr",
                "admin", "Martin", "Alexandre", LocalDate.of(1985, 3, 12),
                Grade.SERGENT, Role.ADMINISTRATEUR));

        gestionnaireCree = userService.create(buildUser("1234567890",
                "gestionnaire@intradef.gouv.fr", "gestionnaire", "Bernard", "Hugo",
                LocalDate.of(1990, 6, 18), Grade.SERGENT, Role.GESTIONNAIRE));

        userService.create(buildUser("2345678901", "formateur@intradef.gouv.fr",
                "formateur", "Dubois", "Thomas", LocalDate.of(1988, 9, 5),
                Grade.SERGENT, Role.FORMATEUR));

        stagiaireCree = userService.create(buildUser("3456789012",
                "stagiaire@intradef.gouv.fr", "stagiaire", "Petit", "Lucas",
                LocalDate.of(2001, 11, 23), Grade.SERGENT, Role.STAGIAIRE));

        String[] prenoms = {
                "Antoine", "Camille", "Julien", "Emma", "Louis", "Lea",
                "Gabriel", "Chloe", "Maxime", "Manon", "Nicolas", "Sarah"
        };
        String[] noms = {
                "Robert", "Richard", "Durand", "Moreau",
                "Simon", "Laurent", "Lefebvre", "Michel"
        };

        // 4 comptes principaux + 96 comptes supplémentaires = 100.
        // Répartition finale : 5 administrateurs, 10 gestionnaires,
        // 15 formateurs et 70 stagiaires.
        for (int i = 5; i <= 100; i++) {
            int index = i - 5;
            String prenom = prenoms[index % prenoms.length];
            String nom = noms[index / prenoms.length];
            String email = (prenom + "." + nom).toLowerCase(Locale.ROOT)
                    + "@intradef.gouv.fr";

            Role role;
            if (i <= 8) {
                role = Role.ADMINISTRATEUR;
            } else if (i <= 17) {
                role = Role.GESTIONNAIRE;
            } else if (i <= 31) {
                role = Role.FORMATEUR;
            } else {
                role = Role.STAGIAIRE;
            }

            userService.create(buildUser(
                    String.format(Locale.ROOT, "900000%04d", i),
                    email, "Test@1234", nom, prenom,
                    LocalDate.of(1985 + index % 20, 1 + index % 12, 1 + index % 28),
                    Grade.SERGENT, role
            ));
        }
    }

    private User buildUser(String nid, String email, String password,
                           String nom, String prenom, LocalDate dateNaissance,
                           Grade grade, Role role) {
        User user = new User();
        user.setNid(nid);
        user.setEmail(email);
        user.setPassword(password);
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setDateNaissance(dateNaissance);
        user.setGrade(grade);
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private void initMaterial() throws ServiceException {
        String[][] unitesCentrales = {
                {"Dell", "OptiPlex 7090", "Intel Core i5", "Intel UHD Graphics"},
                {"HP", "ProDesk 600 G6", "Intel Core i5", "Intel UHD Graphics"},
                {"Lenovo", "ThinkCentre M720s", "Intel Core i5", "Intel UHD Graphics"},
                {"Dell", "Precision 3650", "Intel Core i7", "NVIDIA Quadro P620"},
                {"HP", "EliteDesk 800 G6", "Intel Core i7", "Intel UHD Graphics"},
                {"Lenovo", "ThinkStation P340", "Intel Core i7", "NVIDIA Quadro P1000"}
        };
        String[][] ecrans = {
                {"Dell", "P2422H", "24"},
                {"HP", "E24 G4", "24"},
                {"Lenovo", "ThinkVision T24i-20", "24"},
                {"Samsung", "F24T350", "24"},
                {"LG", "27MP400", "27"},
                {"ASUS", "VA24EHE", "24"}
        };
        String[][] projecteurs = {
                {"Epson", "EB-W06"},
                {"BenQ", "MW560"},
                {"Optoma", "HD146X"},
                {"Acer", "X1328Wi"}
        };

        int numeroUC = 0;
        int numeroEcran = 0;
        int numeroVP = 0;
        LocalDate aujourdHui = LocalDate.now();

        // Production de 250 UC, 200 écrans et 50 vidéoprojecteurs.
        // La localisation et la dotation sont effectuées après les stages.
        for (int numeroSalle = 1; numeroSalle <= 100; numeroSalle++) {
            int nombreUC = numeroSalle <= 50 ? 3 : 2;

            for (int j = 0; j < nombreUC; j++) {
                numeroUC++;
                String[] modele = unitesCentrales[(numeroUC - 1) % unitesCentrales.length];
                LocalDate ajout = aujourdHui.minusMonths(1 + numeroUC % 48);
                UniteCentrale uc = new UniteCentrale();
                remplirMateriel(uc, numeroSerie("UC", numeroUC),
                        modele[1],
                        ajout, ajout.plusYears(3), modele[0], modele[1],
                        etatMateriel(numeroUC));
                uc.setProcesseur(modele[2]);
                uc.setCarteGraphique(modele[3]);
                uniteCentraleService.create(uc);
            }

            for (int j = 0; j < 2; j++) {
                numeroEcran++;
                String[] modele = ecrans[(numeroEcran - 1) % ecrans.length];
                LocalDate ajout = aujourdHui.minusMonths(1 + numeroEcran % 48);
                Ecran ecran = new Ecran();
                remplirMateriel(ecran, numeroSerie("ECR", numeroEcran),
                        modele[1],
                        ajout, ajout.plusYears(3), modele[0], modele[1],
                        etatMateriel(numeroEcran));
                ecran.setTaille(Integer.parseInt(modele[2]));
                ecranService.create(ecran);
            }

            if (numeroSalle > 50) {
                numeroVP++;
                String[] modele = projecteurs[(numeroVP - 1) % projecteurs.length];
                LocalDate ajout = aujourdHui.minusMonths(1 + numeroVP % 48);
                VideoProjecteur vp = new VideoProjecteur();
                remplirMateriel(vp, numeroSerie("VP", numeroVP),
                        modele[1],
                        ajout, ajout.plusYears(3), modele[0], modele[1],
                        etatMateriel(numeroVP));
                videoProjecteurService.create(vp);
            }
        }
    }

    private String numeroSerie(String prefixe, int numero) {
        return String.format(Locale.ROOT, "%s-%04d", prefixe, numero);
    }

    private EtatMateriel etatMateriel(int numero) {
        if (numero % 20 == 0) {
            return EtatMateriel.DEPRECIER;
        }
        if (numero % 10 == 0) {
            return EtatMateriel.EN_MAINTENANCE;
        }
        return EtatMateriel.EN_ETAT;
    }

    private void remplirMateriel(Materiel materiel, String numeroSerie, String nom,
                                 LocalDate dateAjout, LocalDate dateFinGarantie,
                                 String marque, String modele, EtatMateriel etat) {
        materiel.setNumeroSerie(numeroSerie);
        materiel.setNom(nom);
        materiel.setDateAjout(dateAjout);
        materiel.setDateFinGarantie(dateFinGarantie);
        materiel.setMarque(marque);
        materiel.setModele(modele);
        materiel.setEtat(etat);
    }

    private void initBatimentSalle() throws ServiceException {
        String[] nomsBatiments = {"201", "202", "203", "320", "321"};

        for (String nomBatiment : nomsBatiments) {
            Set<Salle> salles = new LinkedHashSet<>();

            for (int numeroSalle = 1; numeroSalle <= 20; numeroSalle++) {
                Set<Materiel> materiels = new LinkedHashSet<>();

                Salle salle = new Salle();
                salle.setNom(String.format(Locale.ROOT, "%s-%03d", nomBatiment, numeroSalle));
                salle.setTypeSalle(numeroSalle == 20 ? TypeSalle.STOCKAGE : TypeSalle.COURS);
                salle.setNbPlaces(numeroSalle == 20 ? 60 : 30);
                salle.setEtat(EtatSalle.DISPONIBLE);
                salle.setMateriels(materiels);

                Salle salleCreee = salleService.create(salle);
                salles.add(salleCreee);
            }

            Batiment batiment = new Batiment();
            batiment.setNom(nomBatiment);
            batiment.setSalles(salles);
            batimentService.create(batiment);
        }
    }

    private void initAlerte() throws ServiceException {
        User signaleur = stagiaireCree;
        User resolveur = gestionnaireCree;

        LocalDateTime maintenant = LocalDateTime.now();
        // salleid: 3
        alerteService.create(buildAlerte(signaleur, maintenant.minusHours(2), null, null, "Ecran sans affichage", "L'ecran reste noir apres le demarrage du poste.", StatutAlerte.EN_ATTENTE,
                materielService.findById(144L).get()));

        // salleid: 1
        alerteService.create(buildAlerte(signaleur, maintenant.minusDays(15), resolveur, maintenant.minusDays(10), "Probleme de demarrage", "Le poste ne demarrait plus. Alimentation remplacee.", StatutAlerte.TRAITEE,
                materielService.findById(1L).get()));
        alerteService.create(buildAlerte(signaleur, maintenant.minusDays(20), resolveur, maintenant.minusDays(19), "Signalement en double", "Signalement annule car le probleme etait deja declare.", StatutAlerte.ANNULEE,
                materielService.findById(2L).get()));

        // salleid: 62
        alerteService.create(buildAlerte(signaleur, maintenant.minusDays(21), null, null, "Signalement en double", "d", StatutAlerte.EN_ATTENTE,
                materielService.findById(304L).get()));
        alerteService.create(buildAlerte(signaleur, maintenant.minusDays(24), null, null, "Signalement en double", "d", StatutAlerte.EN_ATTENTE,
                materielService.findById(136L).get()));
        alerteService.create(buildAlerte(signaleur, maintenant.minusDays(25), null, null, "Signalement en double", "d", StatutAlerte.EN_ATTENTE,
                materielService.findById(327L).get()));

        // salleid: 19
        alerteService.create(buildAlerte(signaleur, maintenant.minusDays(22), null, null, "Signalement en double", "d", StatutAlerte.EN_ATTENTE,
                materielService.findById(231L).get()));
        alerteService.create(buildAlerte(signaleur, maintenant.minusDays(23), null, null, "Signalement en double", "d", StatutAlerte.EN_ATTENTE,
                materielService.findById(471L).get()));

    }

    private Alerte buildAlerte(User signaleur, LocalDateTime dateSignalement,
                               User resolveur, LocalDateTime dateResolution,
                               String typeProbleme, String description,
                               StatutAlerte statut, Materiel materiel) {
        Alerte alerte = new Alerte();
        alerte.setSignaleur(signaleur);
        alerte.setDateSignalement(dateSignalement);
        alerte.setResolveur(resolveur);
        alerte.setDateResolution(dateResolution);
        alerte.setTypeProbleme(typeProbleme);
        alerte.setDescription(description);
        alerte.setStatut(statut);
        alerte.getMateriels().add(materiel);
        return alerte;
    }

    private void rattacherSallesAuxBatiments(List<Salle> salles) {
        Map<Long, Batiment> proprietaires = new LinkedHashMap<>();
        List<Batiment> batiments = new ArrayList<>(batimentService.findAll());
        for (Batiment batiment : batiments) {
            for (Salle salle : batiment.getSalles()) {
                Batiment precedent = proprietaires.put(salle.getId(), batiment);
                if (precedent != null && !precedent.getId().equals(batiment.getId())) {
                    throw new IllegalStateException("Salle rattachee a deux batiments : " + salle.getNom());
                }
            }
        }
        for (Batiment batiment : batiments) {
            for (Salle salle : salles) {
                if (salle.getNom().startsWith(batiment.getNom() + "-")
                        && !proprietaires.containsKey(salle.getId())) {
                    // Batiment.salles porte la FK batiment_id : pas de setBatiment fictif.
                    batiment.getSalles().add(salle);
                    proprietaires.put(salle.getId(), batiment);
                }
            }
        }
    }

    private List<Stage> initStagesEtGroupes(List<User> utilisateurs, List<Salle> salles) {
        List<Stage> stages = new ArrayList<>(stageService.findAll());
        Set<Long> stagiairesDejaInscrits = new HashSet<>();
        Set<Long> sallesOccupees = new HashSet<>();
        for (Stage stage : stages) {
            for (User stagiaire : stage.getStagiaires()) {
                if (!stagiairesDejaInscrits.add(stagiaire.getId())) {
                    throw new IllegalStateException("Stagiaire inscrit a plusieurs stages : " + stagiaire.getNid());
                }
            }
            if (stage.getSalleCours() != null) {
                sallesOccupees.add(stage.getSalleCours().getId());
            }
        }

        List<User> disponibles = utilisateurs.stream()
                .filter(u -> u.getRole() == Role.STAGIAIRE && u.isActive())
                .filter(u -> !stagiairesDejaInscrits.contains(u.getId()))
                // Le compte de démonstration est inclus dans le premier groupe.
                .sorted(Comparator.comparing((User u) -> !"stagiaire@intradef.gouv.fr".equals(u.getEmail()))
                        .thenComparing(User::getNid))
                .toList();

        String[] codes = {"WB47", "WB49", "WB50", "WB51"};
        List<Stage> groupes = new ArrayList<>();
        int index = 0;
        for (String code : codes) {
            Stage stage = stages.stream().filter(s -> code.equals(s.getCode())).findFirst().orElse(null);
            if (stage == null && index >= disponibles.size()) {
                continue;
            }
            Salle salle = stage != null ? stage.getSalleCours() : null;
            if (salle == null) {
                salle = salles.stream()
                        .filter(s -> s.getTypeSalle() == TypeSalle.COURS)
                        .filter(s -> s.getEtat() == EtatSalle.DISPONIBLE && s.getNbPlaces() >= 20)
                        .filter(s -> !sallesOccupees.contains(s.getId()))
                        .findFirst().orElseThrow(() -> new IllegalStateException("Aucune salle de cours disponible."));
                sallesOccupees.add(salle.getId());
            }
            if (salle.getTypeSalle() != TypeSalle.COURS) {
                throw new IllegalStateException("Un stage doit occuper une salle de cours : " + code);
            }

            boolean nouveau = stage == null;
            if (nouveau) {
                stage = buildStage(code, LocalDate.now().minusMonths(2),
                        LocalDate.now().plusMonths(1), salle, StatutStage.EN_COURS);
            } else {
                stage.setSalleCours(salle);
                if (stage.getStatut() == StatutStage.A_AFFECTER) {
                    stage.setStatut(StatutStage.EN_COURS);
                }
            }
            int capaciteGroupe = Math.min(20, salle.getNbPlaces());
            while (stage.getStagiaires().size() < capaciteGroupe && index < disponibles.size()) {
                stage.getStagiaires().add(disponibles.get(index++));
            }
            if (stage.getStagiaires().size() > salle.getNbPlaces()) {
                throw new IllegalStateException("Capacite de salle depassee pour " + code);
            }
            if (nouveau) {
                stage = stageService.create(stage);
                stages.add(stage);
            }
            // Pour un stage existant, la collection est gérée dans la transaction
            // de run(). Sa modification persiste users.stage_id au commit.
            groupes.add(stage);
        }
        if (index < disponibles.size()) {
            throw new IllegalStateException("Plus de stagiaires que de places dans les quatre groupes de test.");
        }
        if (stages.stream().noneMatch(s -> "WB48".equals(s.getCode()))) {
            stageService.create(buildStage("WB48", LocalDate.now().plusDays(8),
                    LocalDate.now().plusMonths(8), null, StatutStage.A_AFFECTER));
        }
        return groupes;
    }

    private void affecterMateriels(List<Stage> groupes, List<Salle> salles, List<Materiel> materiels) {
        Map<Long, Salle> localisations = new LinkedHashMap<>();
        for (Salle salle : salles) {
            for (Materiel materiel : salle.getMateriels()) {
                Salle precedente = localisations.put(materiel.getId(), salle);
                if (precedente != null && !precedente.getId().equals(salle.getId())) {
                    throw new IllegalStateException("Materiel present dans deux salles : " + materiel.getNumeroSerie());
                }
            }
        }
        // Les salles de tous les stages sont protégées : aucun prélèvement
        // dans une autre salle de formation, même hors des groupes de test.
        Set<Long> sallesStages = new HashSet<>();
        for (Stage stage : stageService.findAll()) {
            if (stage.getSalleCours() != null) {
                sallesStages.add(stage.getSalleCours().getId());
            }
        }

        int indexStagiaire = 0;
        for (Stage stage : groupes) {
            Salle salle = stage.getSalleCours();
            List<User> stagiaires = stage.getStagiaires().stream()
                    .sorted(Comparator.comparing((User u) -> !"stagiaire@intradef.gouv.fr".equals(u.getEmail()))
                            .thenComparing(User::getNid))
                    .toList();
            for (User stagiaire : stagiaires) {
                // 1 stagiaire sur 7 sans dotation : 60 équipés sur 70 à neuf.
                boolean equiper = indexStagiaire++ % 7 != 6;
                boolean dejaEquipe = materiels.stream().anyMatch(m ->
                        m.getStagiaire() != null && m.getStagiaire().getId().equals(stagiaire.getId()));
                if (!equiper || dejaEquipe) {
                    continue;
                }
                Materiel uc = trouverMateriel(UniteCentrale.class, salle, materiels, localisations, sallesStages);
                Materiel ecran = trouverMateriel(Ecran.class, salle, materiels, localisations, sallesStages);
                if (uc == null || ecran == null) {
                    throw new IllegalStateException("Pas assez d'UC/ecrans disponibles pour " + stage.getCode());
                }
                localiser(uc, salle, localisations);
                localiser(ecran, salle, localisations);
                // Materiel.stagiaire porte la FK : User n'a pas de setMateriels.
                uc.setStagiaire(stagiaire);
                ecran.setStagiaire(stagiaire);
            }
        }

        // Tout le reliquat reçoit aussi une salle. Aucun matériel n'est
        // laissé sans localisation et un matériel a un seul propriétaire.
        if (!materiels.isEmpty() && salles.isEmpty()) {
            throw new IllegalStateException("Aucune salle pour localiser les materiels.");
        }
        Map<Long, Salle> sallesStagiaires = new LinkedHashMap<>();
        for (Stage stage : stageService.findAll()) {
            if (stage.getSalleCours() != null) {
                for (User stagiaire : stage.getStagiaires()) {
                    sallesStagiaires.put(stagiaire.getId(), stage.getSalleCours());
                }
            }
        }
        int indexSalle = 0;
        for (Materiel materiel : materiels) {
            Salle salleStagiaire = materiel.getStagiaire() == null ? null
                    : sallesStagiaires.get(materiel.getStagiaire().getId());
            if (materiel.getStagiaire() != null && salleStagiaire == null) {
                throw new IllegalStateException("Materiel attribue a un stagiaire sans salle de stage : "
                        + materiel.getNumeroSerie());
            }
            if (salleStagiaire != null) {
                localiser(materiel, salleStagiaire, localisations);
            } else if (!localisations.containsKey(materiel.getId())) {
                localiser(materiel, salles.get(indexSalle++ % salles.size()), localisations);
            }
        }
    }

    private Materiel trouverMateriel(Class<? extends Materiel> type, Salle cible,
                                    List<Materiel> materiels, Map<Long, Salle> localisations,
                                    Set<Long> sallesStages) {
        // On utilise d'abord le matériel déjà dans la salle du stage.
        for (int passage = 0; passage < 2; passage++) {
            for (Materiel materiel : materiels) {
                if (!type.isInstance(materiel) || materiel.getEtat() != EtatMateriel.EN_ETAT
                        || materiel.getStagiaire() != null) {
                    continue;
                }
                Salle origine = localisations.get(materiel.getId());
                boolean dansSalle = origine != null && origine.getId().equals(cible.getId());
                if (passage == 0 && dansSalle) {
                    return materiel;
                }
                if (passage == 1 && (origine == null
                        || (!sallesStages.contains(origine.getId()) && origine.getMateriels().size() > 1))) {
                    return materiel;
                }
            }
        }
        return null;
    }

    private void localiser(Materiel materiel, Salle destination, Map<Long, Salle> localisations) {
        Salle origine = localisations.get(materiel.getId());
        if (origine != null && origine.getId().equals(destination.getId())) {
            return;
        }
        if (origine != null) {
            origine.getMateriels().remove(materiel);
        }
        // Salle.materiels porte la FK salle_id, pas Materiel.setSalle().
        destination.getMateriels().add(materiel);
        localisations.put(materiel.getId(), destination);
    }

    private Stage buildStage(String code, LocalDate dateDebut, LocalDate dateFin,
                             Salle salleCours, StatutStage statut) {
        Stage stage = new Stage();
        stage.setCode(code);
        stage.setDateDebut(dateDebut);
        stage.setDateFin(dateFin);
        stage.setSalleCours(salleCours);
        stage.setStatut(statut);
        return stage;
    }
}