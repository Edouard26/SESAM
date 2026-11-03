package net.ent.etnc.backend.models.enums;

/**
 * Énumération des différents statuts possibles d'une alerte.
 *
 * <p>Permet de suivre l'évolution du traitement d'une alerte
 * au sein de l'application, depuis sa création jusqu'à sa résolution
 * ou son annulation.</p>
 */
public enum StatutAlerte {
    EN_ATTENTE,
    TRAITEE,
    ANNULEE
}
