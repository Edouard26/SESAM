package net.ent.etnc.backend.models.enums;

/**
 * Énumération des différents statuts possibles d'un stage.
 *
 * <p>Permet de suivre le cycle de vie d'un stage, depuis l'attente
 * d'une affectation jusqu'à sa clôture.</p>
 */
public enum StatutStage {
    A_AFFECTER,
    AFFECTER,
    EN_COURS,
    CLOTURER
}
