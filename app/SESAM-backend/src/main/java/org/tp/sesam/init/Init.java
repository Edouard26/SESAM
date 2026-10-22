package net.ent.etnc.backend.init;

import net.ent.etnc.backend.models.entities.*;
import net.ent.etnc.backend.models.enums.*;
import net.ent.etnc.backend.services.*;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Jeu de test SESAM pour une base initialement vide :
 * 500 matériels (250 UC, 200 écrans, 50 vidéoprojecteurs),
 * 5 bâtiments, 100 salles et 100 utilisateurs fictifs.
 *
 * Chaque catégorie est initialisée uniquement si son compteur est nul,
 * comme dans la version initiale. Les données existantes ne sont pas complétées.
 * Les matériels nouvellement créés sont répartis à raison de 5 par salle.
 *
 * Comptes principaux : admin, gestionnaire, formateur, stagiaire
 * suivis de @intradef.gouv.fr. Mots de passe : respectivement
 * admin, gestionnaire, formateur, stagiaire.
 * Les 96 autres comptes utilisent Test@1234.
 * Les noms commerciaux sont réels ; les numéros de série sont fictifs.
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

    // Références retournées par les services : aucun ID supposé pour les créations.
    private final List<Materiel> materielsCrees = new ArrayList<>();
    private User gestionnaireCree;
    private User stagiaireCree;
    private Salle premiereSalleCreee;

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
    public void run(String... args) throws Exception {
        if (userService.count() == 0) {
            initUsers();
        }
        if (materielService.count() == 0) {
            initMaterial();
        }
        if (batimentService.count() == 0) {
            initBatimentSalle();
        }
        if (alerteService.count() == 0) {
            initAlerte();
        }
        if (stageService.count() == 0) {
            initStage();
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

        // Un lot de 5 matériels par salle :
        // salles 1 à 50 : 3 UC + 2 écrans ;
        // salles 51 à 100 : 2 UC + 2 écrans + 1 vidéoprojecteur.
        for (int numeroSalle = 1; numeroSalle <= 100; numeroSalle++) {
            int nombreUC = numeroSalle <= 50 ? 3 : 2;

            for (int j = 0; j < nombreUC; j++) {
                numeroUC++;
                String[] modele = unitesCentrales[(numeroUC - 1) % unitesCentrales.length];
                LocalDate ajout = aujourdHui.minusMonths(1 + numeroUC % 48);
                UniteCentrale uc = new UniteCentrale();
                remplirMateriel(uc, numeroSerie("UC", numeroUC),
                        modele[0] + " " + modele[1] + " - UC " + numeroUC,
                        ajout, ajout.plusYears(3), modele[0], modele[1],
                        etatMateriel(numeroUC));
                uc.setProcesseur(modele[2]);
                uc.setCarteGraphique(modele[3]);
                materielsCrees.add(uniteCentraleService.create(uc));
            }

            for (int j = 0; j < 2; j++) {
                numeroEcran++;
                String[] modele = ecrans[(numeroEcran - 1) % ecrans.length];
                LocalDate ajout = aujourdHui.minusMonths(1 + numeroEcran % 48);
                Ecran ecran = new Ecran();
                remplirMateriel(ecran, numeroSerie("ECR", numeroEcran),
                        modele[0] + " " + modele[1] + " - Ecran " + numeroEcran,
                        ajout, ajout.plusYears(3), modele[0], modele[1],
                        etatMateriel(numeroEcran));
                ecran.setTaille(Integer.parseInt(modele[2]));
                materielsCrees.add(ecranService.create(ecran));
            }

            if (numeroSalle > 50) {
                numeroVP++;
                String[] modele = projecteurs[(numeroVP - 1) % projecteurs.length];
                LocalDate ajout = aujourdHui.minusMonths(1 + numeroVP % 48);
                VideoProjecteur vp = new VideoProjecteur();
                remplirMateriel(vp, numeroSerie("VP", numeroVP),
                        modele[0] + " " + modele[1] + " - Videoprojecteur " + numeroVP,
                        ajout, ajout.plusYears(3), modele[0], modele[1],
                        etatMateriel(numeroVP));
                materielsCrees.add(videoProjecteurService.create(vp));
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
        int indexMateriel = 0;

        for (String nomBatiment : nomsBatiments) {
            Set<Salle> salles = new LinkedHashSet<>();

            for (int numeroSalle = 1; numeroSalle <= 20; numeroSalle++) {
                Set<Materiel> materiels = new LinkedHashSet<>();
                // Si les matériels existaient avant ce lancement, ils ne sont
                // pas réaffectés : seules les nouvelles créations sont réparties.
                for (int j = 0; j < 5 && indexMateriel < materielsCrees.size(); j++) {
                    materiels.add(materielsCrees.get(indexMateriel++));
                }

                Salle salle = new Salle();
                salle.setNom(String.format(Locale.ROOT, "%s-%03d", nomBatiment, numeroSalle));
                salle.setTypeSalle(numeroSalle == 20 ? TypeSalle.STOCKAGE : TypeSalle.COURS);
                salle.setNbPlaces(numeroSalle == 20 ? 60 : 30);
                salle.setEtat(EtatSalle.DISPONIBLE);
                salle.setMateriels(materiels);

                Salle salleCreee = salleService.create(salle);
                salles.add(salleCreee);
                if (premiereSalleCreee == null) {
                    premiereSalleCreee = salleCreee;
                }
            }

            Batiment batiment = new Batiment();
            batiment.setNom(nomBatiment);
            batiment.setSalles(salles);
            batimentService.create(batiment);
        }
    }

    private void initAlerte() throws ServiceException {
        // Les recherches par ID ne servent que de repli pour une base déjà
        // partiellement initialisée avec l'ancien jeu de test.
        User signaleur = stagiaireCree != null ? stagiaireCree
                : userService.findById(4L).orElseThrow(() ->
                new IllegalStateException("Utilisateur signaleur introuvable (ancien ID 4)."));
        User resolveur = gestionnaireCree != null ? gestionnaireCree
                : userService.findById(2L).orElseThrow(() ->
                new IllegalStateException("Utilisateur resolveur introuvable (ancien ID 2)."));

        LocalDateTime maintenant = LocalDateTime.now();
        alerteService.create(buildAlerte(signaleur, maintenant.minusHours(2),
                null, null, "Ecran sans affichage",
                "L'ecran reste noir apres le demarrage du poste.", StatutAlerte.EN_ATTENTE));
        alerteService.create(buildAlerte(signaleur, maintenant.minusDays(15),
                resolveur, maintenant.minusDays(10), "Probleme de demarrage",
                "Le poste ne demarrait plus. Alimentation remplacee.", StatutAlerte.TRAITEE));
        alerteService.create(buildAlerte(signaleur, maintenant.minusDays(20),
                resolveur, maintenant.minusDays(19), "Signalement en double",
                "Signalement annule car le probleme etait deja declare.", StatutAlerte.ANNULEE));
    }

    private Alerte buildAlerte(User signaleur, LocalDateTime dateSignalement,
                               User resolveur, LocalDateTime dateResolution,
                               String typeProbleme, String description,
                               StatutAlerte statut) {
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

    private void initStage() throws ServiceException {
        Salle salle = premiereSalleCreee != null ? premiereSalleCreee
                : salleService.findById(1L).orElseThrow(() ->
                new IllegalStateException("Salle de cours introuvable (ancien ID 1)."));

        stageService.create(buildStage("WB47", LocalDate.now().minusMonths(8),
                LocalDate.now().plusMonths(1), salle, StatutStage.EN_COURS));
        stageService.create(buildStage("WB48", LocalDate.now().plusDays(8),
                LocalDate.now().plusMonths(8), null, StatutStage.A_AFFECTER));
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
