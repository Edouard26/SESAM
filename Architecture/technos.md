# CTT — Cadre de Cohérence Technique

## 1. Objet

Ce document décrit les principaux choix techniques retenus pour l'application et définit le socle technique utilisé pour son développement, son exécution et son déploiement.

L'application repose sur une architecture web séparant le front-end, le back-end et la base de données.

```text
Utilisateur
    |
    v
React / TypeScript
    |
    | HTTP / REST + WebSocket
    v
Spring Boot
    |
    | JPA / Hibernate
    v
PostgreSQL
```

L'ensemble des composants destinés au déploiement est exécuté dans des conteneurs Docker sur une machine ou une machine virtuelle.

---

## 2. Architecture technique

L'architecture retenue est une architecture **front-end / back-end / base de données**.

### 2.1 Front-end

Le front-end est une application web développée avec :

- **React** : `19.2.8`
- **TypeScript** : `~6.0.2`
- **Vite** : `8.3.0`

Les échanges avec l'API back-end sont réalisés avec **Axios** et les communications temps réel utilisent **WebSocket**.

### 2.2 Back-end

Le back-end est développé en **Java 21** avec **Spring Boot 4.0.6**.

Les principales briques utilisées sont :

- **Spring Web** : exposition de l'API HTTP/REST
- **Spring Data JPA** : accès aux données
- **Spring Validation** : validation des données reçues
- **Spring Security** : sécurisation de l'application
- **Spring Actuator** : supervision et exposition d'informations de fonctionnement
- **Lombok** : réduction du code répétitif
- **JJWT 0.13.0** : gestion des JSON Web Tokens

### 2.3 Base de données

La base de données est **PostgreSQL 16**, exécutée à partir de l'image Docker :

```text
postgres:16-alpine
```

Le back-end accède à la base de données via **JPA/Hibernate**.

---

## 3. Technologies front-end

### Rôle des principales bibliothèques

| Technologie | Version | Utilisation |
|---|---|---|
| React | 19.2.8 | Construction de l'interface utilisateur |
| React DOM | 19.2.8 | Rendu de l'application React dans le navigateur |
| React Router | 7.18.4 | Gestion de la navigation entre les vues |
| TypeScript | ~6.0.2 | Typage statique du code front-end |
| Vite | 8.3.0 | Serveur de développement et outil de build |
| Axios | ^1.20.0 | Communication HTTP avec le back-end |
| Bootstrap | ^5.3.8 | Mise en forme et composants graphiques |
| Bootstrap Icons | ^1.13.1 | Icônes de l'interface |
| ESLint | ^10.10.0 | Analyse statique et contrôle du code |
| TypeScript ESLint | ^8.69.0 | Intégration des règles ESLint avec TypeScript |

---

## 4. Technologies back-end

Le projet Maven utilise **Spring Boot 4.0.6** et **Java 21**.

### Composants principaux

| Composant | Version | Rôle |
|---|---|---|
| Spring Boot | 4.0.6 | Framework principal du back-end |
| Java | 21 | Langage et runtime de développement |
| Spring Web | 4.0.6 | Création de l'API REST |
| Spring Data JPA | 4.0.6 | Accès et gestion des données |
| Spring Validation | 4.0.6 | Validation des données entrantes |
| Spring Security | 4.0.6 | Sécurisation de l'application |
| Spring Boot Actuator | 4.0.6 | Supervision et informations de fonctionnement |
| PostgreSQL Driver | — | Connexion entre Spring Boot et PostgreSQL |
| Lombok | — | Réduction du code répétitif |

### Authentification JWT

## 5. Environnement d'exécution

L'infrastructure repose sur **Docker**.

Les composants logiciels sont exécutés sous forme de conteneurs afin de reproduire de manière homogène l'environnement d'exécution.

Les technologies d'exécution utilisées sont :

| Composant | Technologie |
|---|---|
| Conteneurisation | Docker |
| Base de données | PostgreSQL 16 Alpine |
| Runtime Java | Eclipse Temurin 21 JRE |
| Back-end | Spring Boot 4.0.6 |
| Front-end | React / Vite |

---

## 6. Déploiement

L'application est destinée à être déployée sur une **machine physique ou une machine virtuelle disposant de Docker**.

L'environnement de déploiement est organisé autour des différents composants de l'application :

```text
Machine / VM
|
+-- Docker
    |
    +-- Front-end
    |
    +-- Back-end
    |
    +-- PostgreSQL
```

La séparation en conteneurs permet d'isoler les différents composants et de simplifier leur déploiement.

---

## 7. Environnements

Trois environnements sont retenus :

- **Développement**
- **Préproduction**
- **Production**

Les paramètres propres à chaque environnement doivent être séparés de l'application et configurés selon l'environnement d'exécution.

Les informations sensibles telles que les identifiants de base de données ou les secrets JWT ne doivent pas être intégrées directement au code source.

---

## 8. Gestion du code source

Le code source est versionné avec **Git** et hébergé sur **GitHub**.

La stratégie de branches retenue est :

```text
main
 |
 +-- preprod
      |
      +-- develop
```

Les développements sont réalisés sur `develop`, puis intégrés vers `preprod` avant d'être intégrés dans `main`.

---

## 9. Supervision

Le back-end utilise **Spring Boot Actuator**.

Actuator permet notamment d'exposer des informations utiles au suivi de l'état et du fonctionnement de l'application.

Cette brique pourra être utilisée pour contrôler la disponibilité du back-end et faciliter son exploitation sur l'environnement Docker.

---

## 10. Tests

Le projet contient la dépendance **Spring Security Test**, utilisée pour les tests liés à la sécurité Spring.

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

Aucun autre framework de test n'est détaillé dans ce CTT.

---

## 11. Synthèse du socle technique

| Domaine | Choix technique |
|---|---|
| Front-end | React 19 |
| Langage front-end | TypeScript 6 |
| Build front-end | Vite 8 |
| UI | Bootstrap 5 |
| Icônes | Bootstrap Icons |
| Routing | React Router |
| Client HTTP | Axios |
| Communication temps réel | WebSocket |
| Back-end | Spring Boot 4.0.6 |
| Langage back-end | Java 21 |
| API | REST / HTTP |
| Sécurité | Spring Security |
| Authentification | JWT |
| JWT | JJWT 0.13.0 |
| Accès aux données | Spring Data JPA / Hibernate |
| Base de données | PostgreSQL 16 |
| Supervision | Spring Boot Actuator |
| Conteneurisation | Docker |
| Runtime Java | Eclipse Temurin 21 JRE |
| Gestion de versions | Git |
| Hébergement du code | GitHub |
| Environnements | Développement / Préproduction / Production |
| Déploiement | Machine ou VM sous Docker |

---

## 12. Points restant à préciser

Les éléments suivants ne sont pas suffisamment définis dans les informations actuellement disponibles :

1. Le serveur utilisé pour servir le build du front-end en production.
2. La configuration détaillée des connexions WebSocket.
3. L'organisation exacte des fichiers `docker-compose` / Dockerfiles.
4. La stratégie précise de configuration des variables d'environnement.
5. La configuration de PostgreSQL entre les différents environnements.
