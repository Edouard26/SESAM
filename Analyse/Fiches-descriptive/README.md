# Fiches descriptives complémentaires

Ces dix fiches complètent UC01 à UC14 à partir des parcours présents dans `app/Front/src` sur `main` au commit `a63beab779fc87f04891af6ed868023d6cc416a3` (lecture du 4 novembre 2026). Les fiches existantes sont conservées.

Chaque document contient description, acteurs, préconditions, critères d’acceptation, règles de gestion, besoins IHM et non fonctionnels, scénario nominal en colonnes Acteur principal / Système, alternatives, exceptions et références du frontend.

| Cas | Fonctionnalité | Fiche |
| --- | --- | --- |
| UC15 | Se connecter | [UC15-Se_connecter.docx](UC15-Se_connecter.docx) |
| UC16 | Se déconnecter | [UC16-Se_deconnecter.docx](UC16-Se_deconnecter.docx) |
| UC17 | Consulter le tableau de bord | [UC17-Consulter_le_tableau_de_bord.docx](UC17-Consulter_le_tableau_de_bord.docx) |
| UC18 | Modifier un matériel | [UC18-Modifier_un_materiel.docx](UC18-Modifier_un_materiel.docx) |
| UC19 | Créer un utilisateur | [UC19-Creer_un_utilisateur.docx](UC19-Creer_un_utilisateur.docx) |
| UC20 | Modifier un utilisateur | [UC20-Modifier_un_utilisateur.docx](UC20-Modifier_un_utilisateur.docx) |
| UC21 | Créer un master | [UC21-Creer_un_master.docx](UC21-Creer_un_master.docx) |
| UC22 | Créer une règle | [UC22-Creer_une_regle.docx](UC22-Creer_une_regle.docx) |
| UC23 | Modifier une règle | [UC23-Modifier_une_regle.docx](UC23-Modifier_une_regle.docx) |
| UC24 | Supprimer une règle | [UC24-Supprimer_une_regle.docx](UC24-Supprimer_une_regle.docx) |

## Périmètre et points observés

Les rôles sont relevés dans le menu et les composants actuels. Ils ne constituent pas une preuve d’autorisation côté API. Les nouvelles fiches n’introduisent aucune nouvelle fonctionnalité dans l’application.

- UC16 décrit une déconnexion locale : `AuthContext.logout` n’appelle pas le service de déconnexion.
- UC17 décrit le comportement d’erreur actuel : le chargement des statistiques reste visible après un échec.
- UC19 et UC20 tiennent compte de la case `active` marquée `required` dans `UserPopup`, qui empêche de valider ce formulaire avec la case décochée. L’action d’activation/désactivation de la liste est distincte.
- La création individuelle d’un utilisateur est distincte de l’import déjà décrit dans UC14. La modification d’un matériel est distincte de la consultation UC05 et de l’ajout UC08.
- UC22 à UC24 décrivent la gestion du référentiel des règles, distincte du traitement des processus d’une salle dans UC03.
