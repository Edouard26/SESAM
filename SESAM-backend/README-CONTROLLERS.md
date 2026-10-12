# SESAM — contrôleurs et contrats HTTP

## État réel de cette livraison

Les **38 routes demandées** sont déclarées dans **12 contrôleurs**. Les routes techniques
`GET /api/v1/auth/csrf` et `POST /api/v1/auth/refresh` sont conservées : 40 routes au total.
Voir `ROUTES.md` pour l'inventaire exact.

**Il s'agit de la couche contrôleurs et de ses contrats, pas d'un backend métier terminé.**
Les opérations absentes du projet d'origine lèvent `FonctionnaliteNonImplementeeException`,
traduite en HTTP **501**, jusqu'à leur implémentation. Une réponse 501 n'est pas un succès.
Les contrôleurs valident les entrées, transmettent l'identité connectée et délèguent aux services.

## Ce qui est branché sur le modèle actuel

Sous réserve d'avoir complété la configuration et les dépendances d'origine :

| Routes | Traitement présent | Limites explicites |
|---|---|---|
| GET rooms, GET rooms/{roomId} | Repository, DTO, recherche par nom, filtre type, pagination et tri | Lecture globale gestionnaire/admin uniquement ; périmètre stagiaire/formateur à implémenter |
| POST rooms | AbstractService.create + assembler + entité concrète | Champs nom/type uniquement |
| GET materials, GET materials/{materialId} | Repository, DTO, recherche numéro de série/marque, materialId, pagination et tri | Lecture globale gestionnaire/admin uniquement ; autres filtres 501 |
| GET stages, GET stages/{stageId} | Repository, DTO, recherche par nom, pagination et tri | Lecture globale gestionnaire/admin uniquement ; filtres statut/salle 501 |
| GET auth/me | Principal connecté → DTO public | Nécessite une authentification valide |
| POST auth/login, logout, refresh | Contrôleur préexistant adapté aux URL sans slash final et à l'email | RefreshTokenServiceImpl absent du ZIP original : à implémenter |
| Autres routes | Contrat HTTP, DTO et appel du service en place | Cas métier à implémenter, réponse 501 |

Les filtres non pris en charge ne sont jamais ignorés silencieusement.
`LectureScopeService` refuse la lecture globale aux stagiaires et formateurs en renvoyant 501 :
on doit d'abord ajouter les relations qui permettent de déterminer leur périmètre.

## Où lire et modifier

1. `controllers/SalleController.java` : exemple de GET paginé et de POST avec réponse 201.
2. `services/SalleService.java` : contrat des méthodes supplémentaires.
3. `services/impl/SalleServiceImpl.java` : lectures simples déjà implémentées.
4. `services/commons/AbstractService.java` : CRUD conservé, sans duplication.
5. `dtos/SalleDtoAssembler.java` : conversion entité/DTO, avec choix SalleCours/SalleStockage.
6. `dtos/api/ApiDtos.java` : nouveaux filtres, commandes d'action et DTO de réponse.
7. Les autres contrôleurs suivent le même principe.

Dans les interfaces de services, les méthodes `default` sont des **points d'attente temporaires**
qui lèvent 501. Elles permettent aux implémentations CRUD existantes de conserver leur héritage.
Pour réaliser un cas métier, surcharger sa méthode avec `@Override` dans son `ServiceImpl`.
Pour une modification en BDD, ajouter `@Transactional` et conserver l'atomicité de l'opération.
Une fois tous les cas implémentés, supprimer les corps `default` et garder de simples signatures.

Les nouveaux services de règles, alertes, tableau de bord, réinstallations et transferts
sont enregistrés comme beans mais leurs traitements restent volontairement à écrire.

## Contrats choisis pour les paramètres manquants

### Disponibilité d'une salle

`GET /api/v1/rooms?available=true&stageId=12`

Ou :

`GET /api/v1/rooms?available=true&startDate=2026-11-01&endDate=2026-11-20`

`available=true` seul est refusé avec 400 : on ne peut pas vérifier un conflit sans période.
Ne pas fournir stageId et les dates en même temps. Le service devra vérifier les dates du stage,
le type de salle, son état et tous les chevauchements. La réservation finale devra refaire
la vérification dans une transaction afin d'éviter deux affectations simultanées.
Ce traitement n'est pas implémenté dans le modèle actuel : 501 après validation HTTP.

### PATCH

Les PATCH acceptent un objet JSON `application/json`, **pas** un tableau JSON Patch RFC 6902.
L'utilitaire JsonPatchUtils préexistant n'est pas utilisé pour ces routes.

```json
{"version": 0, "roomId": 7}
```

Sur les matériels, stages et utilisateurs, un champ absent ou null signifie « inchangé ».
Un corps sans modification est refusé. Ce contrat ne permet pas d'effacer une relation avec null.
La version est obligatoire ; le service devra comparer la version du client avec l'entité chargée,
puis appliquer les champs et laisser JPA détecter aussi les modifications concurrentes.
Ne pas simplement remplacer la version existante par celle du client.
Les règles utilisent `{"autorise": true}` ou `{"autorise": false}` ; ce choix booléen est un
contrat proposé en l'absence d'entité Regle. Le PATCH crée ou remplace cette règle.

### Signalement

`description` accepte une chaîne vide et au maximum 150 caractères. Le champ doit être présent.
`materialIds` doit être une liste non vide d'identifiants positifs. Les statuts de traitement
acceptés sont `PROCESSED` et `CANCELLED`. Le statut initial et le vocabulaire de type restent
à définir dans le modèle métier.

### Import et export

L'import accepte `{"externalIds": ["SAGAIE-12", "SAGAIE-13"]}` : les références sélectionnées
à récupérer depuis le système externe. Il ne s'agit pas d'un upload CSV. Le format de la source
et son client HTTP restent à implémenter. La liste fournie ne prévoit pas de route pour parcourir
le catalogue externe : cette partie de l'écran nécessitera un contrat supplémentaire si besoin.

Seules les ressources `stages`, `users` et `materials` sont acceptées. Une autre valeur produit 400
pour un appelant autorisé. L'export transmet les paramètres au service, qui devra valider leur
liste blanche, appliquer le périmètre et les filtres, puis produire le fichier sans secrets.
Il devra exporter tous les résultats filtrés ; page/size ne doivent pas tronquer silencieusement
un export. Le contrôleur pose Content-Type, Content-Disposition et Cache-Control: no-store.

## Sécurité

Le rôle n'est jamais accepté comme identité de l'appelant : l'ID vient de `@AuthenticationPrincipal`.
L'identité est transmise aux opérations métier pour vérifier les accès aux objets, pas seulement
les rôles. Les règles de salle doivent toujours filtrer sur le propriétaire connecté.
La priorité Salle > Stage doit être calculée dans le service d'observation, sans modifier
les règles du stage lorsqu'une règle de salle change.

Droits appliqués dans les contrôleurs (base de travail, à aligner si le cahier des charges diffère) :

| Action | Accès |
|---|---|
| Dashboard, salles et matériels en lecture, alertes en lecture/création | Authentifié, puis périmètre du service |
| Création de salle, création/modification/affectation de matériel, réinstallation, traitement d'alerte | Gestionnaire ou administrateur |
| Lecture de stages | Stagiaire, formateur, gestionnaire, administrateur ; périmètre métier requis |
| PATCH de stage | Gestionnaire ou administrateur ; le service doit limiter le gestionnaire à l'affectation de salle |
| Affectation utilisateurs, clôture de stage, relance SAGAIE | Administrateur |
| Processus observés | Formateur ou administrateur |
| Règles de salle | Formateur, ses propres règles uniquement |
| Lecture des règles de stage | Formateur ou administrateur |
| Modification/suppression des règles de stage | Administrateur |
| Gestion utilisateurs | Administrateur |
| Import/export utilisateurs et stages | Administrateur |
| Import/export matériels | Gestionnaire ou administrateur |

Les droits hérités proviennent de ton enum Role : FORMATEUR ajoute STAGIAIRE,
ADMINISTRATEUR ajoute GESTIONNAIRE. L'administrateur ne devient pas formateur automatiquement.

La protection CSRF préexistante sur les routes d'authentification est conservée. Appeler `/auth/csrf`,
garder son cookie, puis utiliser le nom d'en-tête et le jeton retournés pour login/refresh/logout.
Les autres appels utilisent `Authorization: Bearer ...`.
Le logout révoque le refresh token ; le JWT déjà émis reste valable jusqu'à son expiration.
Ne pas retourner une entité User directement, ni un mot de passe ou un hash dans une réponse.

## Travaux métier restant à réaliser

- Ajouter les états/bâtiments des salles et leurs relations aux stages.
- Ajouter type/état/connexion et la salle obligatoire au matériel ; enrichir son DTO de réponse.
- Ajouter le statut et les affectations du stage ; enrichir son DTO de réponse.
- Ajouter les entités d'alertes, de règles et de suivi de réinstallation, avec leurs repositories.
- Implémenter les affectations en transaction, les contrôles de version et la validation complète
  après fusion d'un PATCH. Vérifier utilisateurs actifs et incompatibilités de stages.
- Implémenter la politique utilisateurs : unicité email, hash BCrypt, limites BCrypt en octets,
  droits sur les rôles, maintien d'un administrateur et affectation au stage.
- Implémenter les observations réelles des processus, le calcul de règle effective, les connecteurs
  de réinstallation/SAGAIE/import et les exports.
- La date de fin de garantie et les dates d'un stage peuvent être passées lors d'une consultation
  ou modification historique : réexaminer les @FutureOrPresent du modèle original avant ces mutations.

## Compilation et vérifications

Le projet original conserve son pom.xml (Spring Boot 4.1.1, Java 21).
La compilation Maven a été tentée : **échec avant compilation**, car le dépôt Maven Central
n'était pas joignable dans l'environnement. Le JDK local est 17, donc l'essai utilisait uniquement
une surcharge locale de java.version ; le pom livré reste en Java 21.
Aucune réussite de compilation ou de démarrage de l'application n'est revendiquée.

Vérifications réalisées :
- Présence des 38 routes demandées, plus csrf/refresh, sans doublon méthode + chemin.
- Analyse syntaxique de tous les fichiers Java par le parseur du JDK, sans erreur.
  Cette analyse ne résout pas les dépendances, les types Spring ou le code généré Lombok.
- Relecture des signatures des appels contrôleurs/services.

`ControllersContractTest` contient neuf tests ciblés : identité connectée des règles, validation
avant affectation, réponse 204, suivi 202/Location, 501 explicite, ressource d'import interdite,
limite 150 caractères, période de disponibilité et PATCH vide/false.
**Ces tests sont fournis mais n'ont pas pu être exécutés faute de dépendances.** Ils utilisent des
services simulés et ne valident ni la persistance ni les autorisations Spring Security complètes.

Blocages déjà présents dans le ZIP, à corriger avant de démarrer :
- Dépendances importées dans le code mais absentes du pom : JJWT, RSQL, Jakarta JSON-P et son provider.
- Implémentation de RefreshTokenService absente (bean nécessaire au contrôleur d'authentification).
- Configuration de datasource et pilote JDBC non fournis.

Après correction de ces prérequis, depuis PowerShell avec Java 21 :

```powershell
.\mvnw.cmd -Dtest=ControllersContractTest test
```

`requetes-sesam.http` fournit des exemples à adapter dans le client HTTP IntelliJ.
