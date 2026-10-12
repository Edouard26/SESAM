# SESAM – Front-end

## Présentation

Le front-end de SESAM est une application web développée avec **React et TypeScript**. Elle permet de gérer les équipements informatiques, les salles et les processus, ainsi que de superviser leur état et de consulter les alertes.

## Technologies

- **React** : création de l'interface utilisateur.
- **TypeScript** : typage du code.
- **Vite** : serveur de développement et compilation.
- **React Router** : gestion de la navigation.
- **Bootstrap** : mise en forme et composants d'interface.
- **Axios** : communication avec l'API REST du back-end.

## Fonctionnalités

- Authentification des utilisateurs.
- Tableau de bord.
- Gestion des matériels et des salles.
- Supervision des postes connectés.
- Gestion des processus et des règles.
- Gestion des stages et des alertes.
- Administration de l'application.

## Installation

Installer les dépendances :

```bash
npm install
```

## Lancement en développement

```bash
npm run dev
```

## Compilation

```bash
npm run build
```

Les fichiers compilés sont générés dans le dossier `dist`.

## Communication avec le back-end

Le front-end communique avec le back-end Spring Boot via une API REST. Axios est utilisé pour effectuer les requêtes HTTP.

L'URL de l'API peut être configurée à l'aide des variables d'environnement de Vite.
