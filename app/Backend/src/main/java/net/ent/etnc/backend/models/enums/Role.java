package net.ent.etnc.backend.models.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Rôles des utilisateurs de la plateforme, organisés en hiérarchie cumulative.
 *
 * TODO
 * <p>Chaque rôle hérite des permissions de tous les rôles inférieurs :
 * APPRENANT &lt; CORRECTEUR &lt; FORMATEUR &lt; RESPONSABLE &lt; ADMIN.
 *
 * <p>Spring Security expose les rôles transitifs via {@link User#getAuthorities()},
 * ce qui permet l'utilisation de {@code hasRole('CORRECTEUR')} pour autoriser
 * FORMATEUR, RESPONSABLE et ADMIN par effet de cascade.
 */
public enum Role {

    /** Peut seulement signaler un problème sur son poste */
    STAGIAIRE("Stagiaire"),
    /** Peut signaler un problème sur son poste ou sur un poste de la salle et superviser une salle */
    FORMATEUR("Formateur", STAGIAIRE),
    /** Gère le matériel du parc informatique */
    GESTIONNAIRE("Gestionnaire"),
    /** Accès complet à toutes les fonctionnalités, y compris la gestion des utilisateurs. */
    ADMINISTRATEUR("Administrateur", GESTIONNAIRE);

    @Getter
    private final String libelle;
    @Getter
    private final Set<Role> roles;
    @Getter
    private boolean isTechnical = false;

    Role(String libelle, Role... roles) {
        this.libelle = libelle;
        this.roles = Set.of(roles);
    }

    Role(String libelle, boolean isTechnical, Role... roles) {
        this.libelle = libelle;
        this.isTechnical = isTechnical;
        this.roles = Set.of(roles);
    }

    private static Set<Role> getAllRoles(Role role) {
        Set<Role> roles = new LinkedHashSet<>();
        roles.add(role);
        for (Role r : role.getRoles()) {
            roles.addAll(getAllRoles(r));
        }
        return roles;
    }

    /**
     * Retourne tous les rôles non techniques (exposés à l'utilisateur final).
     */
    public static Role[] valuesNotTechnical() {
        return Arrays.stream(values()).filter(r -> !r.isTechnical()).toArray(Role[]::new);
    }

    /**
     * Vérifie si ce rôle possède (directement ou par héritage) le rôle donné.
     *
     * @param role le rôle à tester
     * @return {@code true} si ce rôle englobe le rôle demandé
     */
    public boolean hasRole(Role role) {
        return this.getAllRoles().contains(role);
    }

    /**
     * Retourne l'ensemble transitif de tous les rôles couverts par ce rôle.
     */
    public Set<Role> getAllRoles() {
        return Role.getAllRoles(this);
    }
}
