package net.ent.etnc.backend.dtos.requests;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO pour la requête de connexion (POST /api/v1/auth/login/).
 * Reçu dans le body de la requête HTTP.
 */
@Getter
@Setter
public class LoginRequest {
    private String email;
    private String password;
}
