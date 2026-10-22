package org.tp.sesam.controllers;
import org.tp.sesam.models.entities.User;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Set;
final class ApiControllerSupport {
    private ApiControllerSupport() { }
    static Long actorId(User user) {
        if (user == null || user.getId() == null)
            throw new AuthenticationCredentialsNotFoundException("Connexion nécessaire");
        return user.getId();
    }
    static String resource(String value) {
        if (!Set.of("stages", "users", "materials").contains(value))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ressource attendue : stages, users ou materials");
        return value;
    }
}
