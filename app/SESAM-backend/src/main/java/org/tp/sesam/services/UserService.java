package org.tp.sesam.services;

import org.tp.sesam.dtos.*;
import org.tp.sesam.dtos.api.ApiDtos.*;
import org.tp.sesam.models.entities.*;
import org.tp.sesam.services.commons.FonctionnaliteNonImplementeeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Contrat métier appelé par les contrôleurs. */
public interface UserService extends org.tp.sesam.services.commons.Service<User, Long> {
    /** Recherche des utilisateurs. À surcharger dans UserServiceImpl. */
    default Page<UserResponse> rechercher(UserFilter filtre, Pageable pageable, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("UserService.rechercher");
    }

    /** Création avec hash du mot de passe et affectation au stage. À surcharger dans UserServiceImpl. */
    default UserResponse creer(UserCreation dto, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("UserService.creer");
    }

    /** Modification avec règles de rôle et contrôle de version. À surcharger dans UserServiceImpl. */
    default UserResponse modifier(Long userId, UserPatch dto, Long actorId) {
        throw new FonctionnaliteNonImplementeeException("UserService.modifier");
    }

}
