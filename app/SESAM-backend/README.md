# SESAM — Backend

> **État du projet :** contrôleurs en place · traitements métier en cours · démarrage et tests à valider

## Sommaire

- [Avancement](#avancement)
- [Ce qui est déjà en place](#ce-qui-est-déjà-en-place)
- [Organisation du code](#organisation-du-code)
- [Fonctionnement des requêtes](#fonctionnement-des-requêtes)
- [Droits d'accès](#droits-daccès)
- [À terminer](#à-terminer)
- [Vérifications et lancement](#vérifications-et-lancement)

---

## Avancement

J'ai mis en place les **12 contrôleurs** correspondant aux **38 routes** prévues pour la maquette. Avec les routes `/api/v1/auth/csrf` et `/api/v1/auth/refresh`, l'API compte **40 routes**. Le détail est dans `ROUTES.md`.

> **À retenir :** la structure est en place, mais il reste les traitements métier à compléter. Les méthodes non implémentées renvoient **HTTP 501** via `FonctionnaliteNonImplementeeException`.

Les contrôleurs récupèrent les paramètres, valident les DTO et appellent les services. **Les règles métier doivent rester dans les services.**

## Ce qui est déjà en place

| Routes | Traitement présent | Ce qui reste à faire |
|---|---|---|
| `GET /rooms` et `GET /rooms/{roomId}` | Lecture en BDD, recherche par nom, filtre par type, pagination et tri | Limiter les résultats aux salles accessibles au stagiaire ou au formateur |
| `POST /rooms` | Création avec `AbstractService.create` et conversion par l'assembler | Pour l'instant, seuls le nom et le type sont gérés |
| `GET /materials` et `GET /materials/{materialId}` | Lecture en BDD, recherche par numéro de série ou marque, filtre par identifiant, pagination et tri | Ajouter les autres filtres et les droits selon les affectations |
| `GET /stages` et `GET /stages/{stageId}` | Lecture en BDD, recherche par nom, pagination et tri | Ajouter les filtres par statut et salle, et les droits selon les affectations |
| `GET /auth/me` | Retour des informations publiques du compte connecté | Vérifier le fonctionnement avec une authentification valide |
| `POST /auth/login`, `/auth/logout` et `/auth/refresh` | Connexion par email et gestion des tokens côté contrôleur | Intégrer et tester l'implémentation de `RefreshTokenService` |
| Autres routes | Contrôleurs, DTO et appels aux services | Implémenter les traitements métier |

Les chemins du tableau sont précédés de `/api/v1`.

La lecture globale des salles, matériels et stages est actuellement prévue pour le gestionnaire et l'administrateur. Pour le stagiaire et le formateur, `LectureScopeService` renvoie encore 501 : il manque les relations nécessaires pour savoir quelles données leur sont accessibles.

> **Filtres :** un filtre non implémenté renvoie aussi **501**. Il ne doit pas être ignoré et donner l'impression que les résultats sont filtrés.

---

## Organisation du code

Pour suivre le fonctionnement d'une route, je peux partir de la gestion des salles :

| Fichier | Rôle |
|---|---|
| `controllers/SalleController.java` | **Réception** des requêtes HTTP |
| `services/SalleService.java` | **Contrat** des méthodes disponibles |
| `services/impl/SalleServiceImpl.java` | **Traitements métier** propres aux salles |
| `services/commons/AbstractService.java` | **CRUD commun** aux services |
| `dtos/SalleDtoAssembler.java` | **Conversion** entre entité et DTO |
| `dtos/api/ApiDtos.java` | **Filtres et DTO** des nouvelles actions |

J'ai remplacé l'héritage `SalleCours` / `SalleStockage` par une seule entité `Salle` avec un champ `typeSalle`. L'assembler doit donc créer une `Salle` et renseigner son enum `TypeSalle`.

### Compléter un service

Les méthodes `default` des interfaces de services sont **temporaires**. Elles renvoient 501 en attendant leur implémentation dans les classes `ServiceImpl` avec `@Override`.

Les modifications en BDD doivent être réalisées dans une transaction avec `@Transactional`. Une opération qui modifie plusieurs éléments doit être enregistrée entièrement ou annulée entièrement.

Une fois les méthodes implémentées, les corps `default` pourront être supprimés des interfaces pour ne garder que les signatures. Les services de règles, d'alertes, de tableau de bord, de réinstallation et de transfert restent à compléter.

## Fonctionnement des requêtes

### Disponibilité des salles

Pour rechercher une salle disponible, il faut préciser la période du stage. Deux possibilités :

**Avec l'identifiant du stage :**

```http
GET /api/v1/rooms?available=true&stageId=12
```

**Avec les dates :**

```http
GET /api/v1/rooms?available=true&startDate=2026-11-01&endDate=2026-11-20
```

> **Paramètres obligatoires :** `available=true` seul renvoie **400**. Il faut fournir **soit le stage, soit les deux dates**, sans mélanger les deux.

Le service devra vérifier le type de salle, son état et les conflits avec les autres stages sur toute la période. Cette vérification devra être refaite au moment de l'affectation, dans une transaction, pour éviter de réserver la même salle deux fois.

Cette partie n'est pas encore implémentée et renvoie donc 501 après validation des paramètres.

### Modifications avec PATCH

Les routes PATCH acceptent un objet JSON avec les champs à modifier :

```json
{"version": 0, "roomId": 7}
```

| Contenu du PATCH | Comportement |
|---|---|
| Champ absent ou à `null` | Valeur **inchangée** |
| Aucun champ à modifier | Requête **refusée** |
| Relation envoyée à `null` | Relation **conservée**, pas supprimée |

Ces règles s'appliquent aux matériels, stages et utilisateurs.

> **Version obligatoire :** le service devra la comparer à celle de l'entité avant d'appliquer les modifications. JPA devra également détecter une modification concurrente pendant l'enregistrement. Il ne faut pas simplement remplacer la version de l'entité par celle reçue.

Ces routes utilisent `application/json`. Elles n'utilisent pas l'utilitaire `JsonPatchUtils` ni les tableaux d'opérations JSON Patch RFC 6902.

Pour les règles de processus, le format prévu est :

```json
{"autorise": false}
```

Le PATCH doit créer ou remplacer la règle du processus concerné. Le champ booléen `autorise` reste à confirmer lors de la création de l'entité `Regle`.

### Signalements

- **Description :** champ obligatoire, chaîne vide autorisée, **150 caractères maximum**.
- **Matériels :** `materialIds` doit contenir au moins un identifiant positif.
- **Traitement :** statuts `PROCESSED` ou `CANCELLED`.
- **À définir :** statut initial et types de signalement.

### Imports et exports

**Import**

L'import reçoit les références sélectionnées dans le système externe :

```json
{"externalIds": ["SAGAIE-12", "SAGAIE-13"]}
```

Le service devra récupérer les données correspondantes. L'envoi d'un fichier CSV n'est pas prévu dans ce contrat. Le format des données externes et le client HTTP restent à mettre en place. Si l'écran doit afficher le catalogue externe avant la sélection, il faudra également prévoir une route pour le consulter.

Les ressources acceptées sont `stages`, `users` et `materials`. Une autre valeur renvoie 400 pour un utilisateur autorisé à appeler la route.

**Export**

Pour l'export, le service devra vérifier les paramètres autorisés, appliquer les filtres et les droits d'accès, puis générer le fichier. L'export doit contenir **tous les résultats filtrés**, sans être limité par la pagination de l'écran et sans inclure de données secrètes.

Le contrôleur renseigne les en-têtes `Content-Type`, `Content-Disposition` et `Cache-Control: no-store`.

## Droits d'accès

L'identifiant de l'utilisateur vient de `@AuthenticationPrincipal`. Il est transmis au service pour vérifier les droits sur les données concernées.

Les droits actuellement prévus dans les contrôleurs sont les suivants. Ils restent à ajuster si les règles du projet évoluent.

| Action | Accès |
|---|---|
| Tableau de bord, consultation des salles et matériels, consultation et création des alertes | Utilisateur connecté, puis contrôle des données accessibles dans le service |
| Création de salle, création/modification/affectation de matériel, réinstallation, traitement des alertes | Gestionnaire ou administrateur |
| Consultation des stages | Stagiaire, formateur, gestionnaire ou administrateur, selon les données accessibles |
| Modification d'un stage | Gestionnaire ou administrateur ; le gestionnaire doit être limité à l'affectation d'une salle |
| Affectation des utilisateurs, clôture d'un stage, relance SAGAIE | Administrateur |
| Consultation des processus observés | Formateur ou administrateur |
| Gestion des règles de salle | Formateur, uniquement pour ses propres règles |
| Consultation des règles de stage | Formateur ou administrateur |
| Modification ou suppression des règles de stage | Administrateur |
| Gestion des utilisateurs | Administrateur |
| Import/export des utilisateurs et stages | Administrateur |
| Import/export des matériels | Gestionnaire ou administrateur |

Dans l'enum `Role`, le formateur possède aussi les droits du stagiaire, et l'administrateur ceux du gestionnaire. L'administrateur ne possède pas automatiquement les droits du formateur.

> **Priorité des règles : Salle > Stage.** Modifier une règle de salle ne doit pas modifier la règle du stage. Les règles de salle doivent toujours être filtrées avec l'identifiant du formateur connecté.

### Authentification

La protection CSRF est conservée pour l'authentification : appeler `/api/v1/auth/csrf`, conserver le cookie reçu, puis transmettre le jeton dans l'en-tête indiqué pour les appels login, refresh et logout.

Les autres requêtes utilisent `Authorization: Bearer ...`. La déconnexion révoque le refresh token ; le JWT déjà émis reste valable jusqu'à son expiration.

> **Réponses API :** utiliser des DTO et ne jamais exposer directement l'entité `User`, un mot de passe ou son hash.

---

## À terminer

- [ ] **Salles :** ajouter l'état, le bâtiment et les relations avec les stages.
- [ ] **Matériels :** ajouter le type, l'état, la connexion et la salle obligatoire, puis compléter les DTO de réponse.
- [ ] **Stages :** ajouter le statut et les affectations, puis compléter les DTO de réponse.
- [ ] **Entités :** créer les entités et repositories des alertes, règles et réinstallations.
- [ ] **Modifications :** implémenter les affectations en transaction, les contrôles de version et la validation après un PATCH.
- [ ] **Affectations :** vérifier que les utilisateurs sont actifs et que leurs stages sont compatibles.
- [ ] **Utilisateurs :** gérer l'email unique, le hash BCrypt, sa limite en octets, les droits sur les rôles, le maintien d'un administrateur et l'affectation au stage.
- [ ] **Processus :** récupérer les processus réellement observés et calculer la règle applicable.
- [ ] **Services externes :** brancher les réinstallations, SAGAIE, les imports et les exports.
- [ ] **Dates :** revoir les `@FutureOrPresent` pour permettre la consultation et, si autorisée, la modification d'une garantie ou d'un stage terminé.

## Vérifications et lancement

| Élément | Version |
|---|---|
| Spring Boot | **4.1.1** |
| Java configuré dans le POM | **21** |
| Java utilisé lors des derniers lancements locaux | **26.0.1** |


### Lancer les tests

Pour les lancer depuis PowerShell, à la racine du backend :

```powershell
.\mvnw.cmd -Dtest=ControllersContractTest test
```

Le fichier `requetes-sesam.http` contient des exemples à utiliser dans le client HTTP d'IntelliJ.
