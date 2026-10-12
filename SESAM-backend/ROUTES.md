# Routes SESAM

38 routes demandées, plus les deux routes techniques CSRF et refresh conservées.

| Méthode | Route | Contrôleur |
|---|---|---|
| DELETE | `/api/v1/rules/rooms/{roomId}` | `RegleController.java` |
| DELETE | `/api/v1/rules/rooms/{roomId}/{processId}` | `RegleController.java` |
| DELETE | `/api/v1/rules/stages/{stageId}/{processId}` | `RegleController.java` |
| GET | `/api/v1/alerts` | `AlerteController.java` |
| GET | `/api/v1/auth/csrf` | `AuthController.java` |
| GET | `/api/v1/auth/me` | `AuthController.java` |
| GET | `/api/v1/dashboard` | `DashboardController.java` |
| GET | `/api/v1/exports/{resource}` | `ExportController.java` |
| GET | `/api/v1/materials` | `MaterielController.java` |
| GET | `/api/v1/materials/{materialId}` | `MaterielController.java` |
| GET | `/api/v1/processes/materials/{materialId}` | `ProcessusController.java` |
| GET | `/api/v1/processes/rooms/{roomId}` | `ProcessusController.java` |
| GET | `/api/v1/reinstallations/{reinstallationId}` | `ReinstallationController.java` |
| GET | `/api/v1/rooms` | `SalleController.java` |
| GET | `/api/v1/rooms/{roomId}` | `SalleController.java` |
| GET | `/api/v1/rules/rooms/{roomId}` | `RegleController.java` |
| GET | `/api/v1/rules/stages/{stageId}` | `RegleController.java` |
| GET | `/api/v1/stages` | `StageController.java` |
| GET | `/api/v1/stages/{stageId}` | `StageController.java` |
| GET | `/api/v1/users` | `UserController.java` |
| PATCH | `/api/v1/alerts/{alertId}` | `AlerteController.java` |
| PATCH | `/api/v1/materials/{materialId}` | `MaterielController.java` |
| PATCH | `/api/v1/rules/rooms/{roomId}/{processId}` | `RegleController.java` |
| PATCH | `/api/v1/rules/stages/{stageId}/{processId}` | `RegleController.java` |
| PATCH | `/api/v1/stages/{stageId}` | `StageController.java` |
| PATCH | `/api/v1/users/{userId}` | `UserController.java` |
| POST | `/api/v1/alerts` | `AlerteController.java` |
| POST | `/api/v1/auth/login` | `AuthController.java` |
| POST | `/api/v1/auth/logout` | `AuthController.java` |
| POST | `/api/v1/auth/refresh` | `AuthController.java` |
| POST | `/api/v1/imports/{resource}` | `ImportController.java` |
| POST | `/api/v1/materials` | `MaterielController.java` |
| POST | `/api/v1/materials/assignments` | `MaterielController.java` |
| POST | `/api/v1/materials/{materialId}/reinstallations` | `MaterielController.java` |
| POST | `/api/v1/rooms` | `SalleController.java` |
| POST | `/api/v1/rooms/{roomId}/reinstallations` | `SalleController.java` |
| POST | `/api/v1/stages/{stageId}/closure` | `StageController.java` |
| POST | `/api/v1/stages/{stageId}/notifications/sagaie/retry` | `StageController.java` |
| POST | `/api/v1/stages/{stageId}/users` | `StageController.java` |
| POST | `/api/v1/users` | `UserController.java` |
