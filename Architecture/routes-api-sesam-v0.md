# Routes API SESAM

Routes proposées pour alimenter les écrans et actions de la maquette interactive SESAM. Les noms de paramètres et les valeurs de statut sont à harmoniser avec les DTO du backend.

## Authentification

La maquette publiée simule le rôle avec un sélecteur : ces routes sont donc nécessaires à l'application réelle, pas à la démonstration actuelle.

```http
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
GET    /api/v1/auth/me
```

## Tableau de bord

```http
GET    /api/v1/dashboard
```

La réponse est adaptée au rôle connecté : statistiques, salles ou matériels accessibles, stage de l'utilisateur et signalements récents.

## Salles

```http
GET    /api/v1/salles
GET    /api/v1/salles/{salleId}
POST   /api/v1/salles
GET    /api/v1/salles/disponibles?dateDebut={dateDebut}&dateFin={dateFin}
GET    /api/v1/salles/{salleId}/materiels
POST   /api/v1/salles/{salleId}/reinstallations
```

Filtres à prévoir pour `GET /api/v1/salles` : `recherche`, `type`, `batiment`, `etat`, `page`, `size`.

La route des salles disponibles doit retourner les salles libres sur toute la période demandée, en excluant notamment les salles de stockage, les salles non disponibles et les conflits avec d'autres stages.

La liste des matériels d'une salle peut également être utilisée par l'écran de supervision, avec les filtres `recherche`, `type`, `materielId`, `etat` et `connexion`.

## Matériels

```http
GET    /api/v1/materiels
GET    /api/v1/materiels/{materielId}
POST   /api/v1/materiels
PATCH  /api/v1/materiels/{materielId}
POST   /api/v1/materiels/affectations
POST   /api/v1/materiels/{materielId}/reinstallations
GET    /api/v1/reinstallations/{reinstallationId}
```

Filtres à prévoir pour `GET /api/v1/materiels` : `recherche`, `type`, `etat`, `salleId`, `batiment`, `page`, `size`.

`POST /api/v1/materiels/affectations` affecte un ou plusieurs matériels disponibles à une salle de destination. Chaque matériel doit toujours être associé à une salle, y compris la salle de stockage.

La réinstallation peut être lancée depuis une salle ou depuis un matériel. La consultation d'une réinstallation permet de suivre son état si l'opération est asynchrone.

## Supervision et processus

```http
GET    /api/v1/materiels/{materielId}/processus
GET    /api/v1/salles/{salleId}/regles-processus
PUT    /api/v1/salles/{salleId}/processus/{processusId}/regle-base
DELETE /api/v1/salles/{salleId}/processus/{processusId}/regle-base
PUT    /api/v1/salles/{salleId}/processus/{processusId}/regle-formateur
DELETE /api/v1/salles/{salleId}/processus/{processusId}/regle-formateur
DELETE /api/v1/salles/{salleId}/regles-formateur
```

- Les processus en cours sont consultés sur le matériel (UC) concerné.
- Les règles de base sont définies à l'échelle de la salle par l'administrateur.
- Les règles personnelles du formateur sont également définies à l'échelle de la salle. Elles prévalent sur les règles de stage et de base pour le processus concerné.
- La maquette affiche la priorité effective **Formateur > Stage > Base**. Les informations de processus doivent donc inclure la règle effective et son origine.
- `DELETE /api/v1/salles/{salleId}/regles-formateur` supprime les règles personnelles du formateur connecté pour cette salle, sans supprimer les règles de base ni celles du stage.

**Écart à noter :** dans la maquette publiée, l'écran de processus permet au formateur de modifier ses règles, mais ne propose pas encore d'écran permettant à l'administrateur de modifier les règles de base. Les routes `regle-base` couvrent le besoin métier indiqué, mais nécessiteront une interface si l'administrateur doit les gérer depuis l'application.

## Stages

```http
GET    /api/v1/stages
GET    /api/v1/stages/{stageId}
PUT    /api/v1/stages/{stageId}/salle
POST   /api/v1/stages/{stageId}/stagiaires
POST   /api/v1/stages/{stageId}/cloture
POST   /api/v1/stages/{stageId}/notifications/sagaie/retry
```

Filtres à prévoir pour `GET /api/v1/stages` : `recherche`, `statut`, `salleId`.

L'affectation d'un stage utilise `GET /api/v1/salles/disponibles` avec ses dates, puis `PUT /api/v1/stages/{stageId}/salle`. L'affectation de stagiaires doit vérifier qu'ils sont actifs et qu'ils ne sont pas affectés à un autre stage incompatible.

La maquette charge les stages depuis une source externe (SAGAIE) ; le bouton « Charger / créer un stage » ouvre l'import. Il n'y a donc pas de route de création manuelle de stage dans cette liste.

## Alertes et signalements

```http
GET    /api/v1/alertes
POST   /api/v1/alertes
PATCH  /api/v1/alertes/{alerteId}
```

Filtres à prévoir pour `GET /api/v1/alertes` : `statut`, et éventuellement la pagination. La création accepte un type, une description et un ou plusieurs identifiants de matériels. Le statut peut être `VALIDEE` ou `REFUSEE` lors du traitement.

La maquette ne présente pas de page de détail dédiée à une alerte : un `GET /api/v1/alertes/{alerteId}` n'est pas indispensable pour la reproduire.

## Utilisateurs

```http
GET    /api/v1/utilisateurs
POST   /api/v1/utilisateurs
PATCH  /api/v1/utilisateurs/{utilisateurId}
```

Filtres à prévoir pour `GET /api/v1/utilisateurs` : `recherche`, `role`, `actif`, `stageId`, `page`, `size`.

La création et la modification couvrent les champs de la fiche utilisateur, l'activation/désactivation du compte et l'affectation éventuelle à un stage. La maquette n'a pas de page de détail utilisateur séparée.

## Imports externes

`{ressource}` prend les valeurs `stages`, `utilisateurs` ou `materiels`.

```http
POST   /api/v1/imports/{ressource}
```

## Exports

```http
GET    /api/v1/exports/stages
GET    /api/v1/exports/utilisateurs
GET    /api/v1/exports/materiels
```

Chaque route génère un export pour la ressource concernée. Les paramètres de recherche et de filtrage de la liste correspondante peuvent être réutilisés pour limiter les données exportées. Le format et le nom du fichier sont définis par la réponse du backend, par exemple un fichier CSV.
