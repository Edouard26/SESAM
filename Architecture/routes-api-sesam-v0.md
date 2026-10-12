# Routes API SESAM

Routes proposées pour alimenter les écrans et les actions de la maquette interactive SESAM. Les noms de paramètres et les valeurs de statut sont à harmoniser avec les DTO du backend.

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
GET    /api/v1/rooms
GET    /api/v1/rooms/{roomId}
POST   /api/v1/rooms
GET    /api/v1/rooms/available?startDate={startDate}&endDate={endDate}
GET    /api/v1/rooms/{roomId}/materials
POST   /api/v1/rooms/{roomId}/reinstallations
```

Filtres à prévoir pour `GET /api/v1/rooms` : `search`, `type`, `building`, `state`, `page`, `size`.

La route des salles disponibles doit retourner les salles libres sur toute la période demandée, en excluant notamment les salles de stockage, les salles non disponibles et les conflits avec d'autres stages.

La liste des matériels d'une salle peut également être utilisée pour l'écran de supervision, avec les filtres `search`, `type`, `materialId`, `state` et `connection`.

## Matériels

```http
GET    /api/v1/materials
GET    /api/v1/materials/{materialId}
POST   /api/v1/materials
PATCH  /api/v1/materials/{materialId}
POST   /api/v1/materials/assignments
POST   /api/v1/materials/{materialId}/reinstallations
GET    /api/v1/reinstallations/{reinstallationId}
```

Filtres à prévoir pour `GET /api/v1/materials` : `search`, `type`, `state`, `roomId`, `building`, `page`, `size`.

`POST /api/v1/materials/assignments` affecte un ou plusieurs matériels à une salle de destination. Chaque matériel doit toujours être associé à une salle, y compris la salle de stockage.

La réinstallation peut être lancée depuis une salle ou depuis un matériel. La consultation d'une réinstallation permet de suivre son état si l'opération est asynchrone.

## Processus

```http
GET    /api/v1/processes/rooms/{roomId}
GET    /api/v1/processes/materials/{materialId}
```

- `GET /api/v1/processes/rooms/{roomId}` consulte les processus observés sur les matériels de la salle.
- `GET /api/v1/processes/materials/{materialId}` consulte les processus observés sur un matériel donné, notamment une UC.
- Les résultats peuvent inclure la règle effective et son origine, mais ces routes ne servent pas à modifier les règles.

## Règles de processus

```http
GET    /api/v1/rules/rooms/{roomId}
PUT    /api/v1/rules/rooms/{roomId}/{processId}
DELETE /api/v1/rules/rooms/{roomId}/{processId}
DELETE /api/v1/rules/rooms/{roomId}
GET    /api/v1/rules/stages/{stageId}
PUT    /api/v1/rules/stages/{stageId}/{processId}
DELETE /api/v1/rules/stages/{stageId}/{processId}
```

- `GET /api/v1/rules/stages/{stageId}` consulte les règles de référence définies pour le stage.
- Les routes `PUT` et `DELETE` sous `/rules/stages/{stageId}` créent/remplacent ou suppriment la règle du stage pour un processus donné. Ces règles servent de base lorsqu'aucune règle n'est définie pour la salle.
- `GET /api/v1/rules/rooms/{roomId}` consulte les règles ajoutées à la salle par le formateur connecté.
- Les routes `PUT` et `DELETE` sous `/rules/rooms/{roomId}` créent/remplacent ou suppriment la règle du formateur connecté pour un processus donné. Le `DELETE` sur `/api/v1/rules/rooms/{roomId}` supprime toutes les règles du formateur connecté pour cette salle.
- Il n'existe pas de niveau distinct de « règle de base » : **la règle du stage est la règle de référence**, et la règle de salle la surcharge pour le processus concerné.
- La priorité effective est **Salle > Stage**. Le backend doit retourner, avec chaque processus lorsque c'est utile, la règle effective et son origine.
- Les règles sont associées à leur niveau respectif : les modifications des règles de salle ne doivent pas modifier les règles du stage, et inversement.

## Stages

```http
GET    /api/v1/stages
GET    /api/v1/stages/{stageId}
PUT    /api/v1/stages/{stageId}/room
POST   /api/v1/stages/{stageId}/trainees
POST   /api/v1/stages/{stageId}/closure
POST   /api/v1/stages/{stageId}/notifications/sagaie/retry
```

Filtres à prévoir pour `GET /api/v1/stages` : `search`, `status`, `roomId`.

L'affectation d'un stage utilise `GET /api/v1/rooms/available` avec ses dates, puis `PUT /api/v1/stages/{stageId}/room`. L'affectation de stagiaires doit vérifier qu'ils sont actifs et qu'ils ne sont pas affectés à un autre stage incompatible.

La maquette charge les stages depuis une source externe (SAGAIE) ; le bouton « Charger / créer un stage » ouvre l'import. Il n'y a donc pas de route de création manuelle de stage dans cette liste.

## Alertes et signalements

```http
GET    /api/v1/alerts
POST   /api/v1/alerts
PATCH  /api/v1/alerts/{alertId}
```

Filtres à prévoir pour `GET /api/v1/alerts` : `status`, et éventuellement la pagination. La création accepte un type, une description et un ou plusieurs identifiants de matériels. La modification permet notamment de traiter le signalement avec le statut `VALIDEE` ou `REFUSEE`.

La maquette ne présente pas de page de détail dédiée à une alerte : un `GET /api/v1/alerts/{alertId}` n'est pas indispensable pour la reproduire.

## Utilisateurs

```http
GET    /api/v1/users
POST   /api/v1/users
PATCH  /api/v1/users/{userId}
```

Filtres à prévoir pour `GET /api/v1/users` : `search`, `role`, `active`, `stageId`, `page`, `size`.

La création et la modification couvrent les champs de la fiche utilisateur, l'activation/désactivation du compte et l'affectation éventuelle à un stage. La maquette n'a pas de page de détail utilisateur séparée.

## Imports externes

`{resource}` prend les valeurs `stages`, `users` ou `materials`.

```http
POST   /api/v1/imports/{resource}
```

## Exports

`{resource}` prend les valeurs `stages`, `users` ou `materials`.

```http
GET    /api/v1/exports/{resource}
```

Cette route génère un export pour la ressource concernée. Les paramètres de recherche et de filtrage de la liste correspondante peuvent être réutilisés pour limiter les données exportées. Le format et le nom du fichier sont définis par la réponse du backend, par exemple un fichier CSV.
