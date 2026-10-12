package org.tp.sesam.models.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Rôles de l'application.
 *
 * FORMATEUR possède aussi les droits de STAGIAIRE.
 * ADMINISTRATEUR possède aussi les droits de GESTIONNAIRE.
 */
public enum Role {

    STAGIAIRE,

    FORMATEUR,

    GESTIONNAIRE,

    ADMINISTRATEUR;

    public Collection<SimpleGrantedAuthority> getAuthorities() {

        return switch (this) {
            case STAGIAIRE -> List.of(
                    new SimpleGrantedAuthority("ROLE_STAGIAIRE")
            );

            case FORMATEUR -> List.of(
                    new SimpleGrantedAuthority("ROLE_FORMATEUR"),
                    new SimpleGrantedAuthority("ROLE_STAGIAIRE")
            );

            case GESTIONNAIRE -> List.of(
                    new SimpleGrantedAuthority("ROLE_GESTIONNAIRE")
            );

            case ADMINISTRATEUR -> List.of(
                    new SimpleGrantedAuthority("ROLE_ADMINISTRATEUR"),
                    new SimpleGrantedAuthority("ROLE_GESTIONNAIRE")
            );
        };
    }
}