package net.ent.etnc.backend.controllers.handlers;

import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestionnaire global des exceptions HTTP pour toute l'application.
 *
 * <p>Stratégie de mapping :
 * <ul>
 *   <li>{@link AccessDeniedException} → 401 si non authentifié, 403 si authentifié sans les droits requis.</li>
 *   <li>{@link ServiceException} → 400 Bad Request avec le message métier.</li>
 *   <li>{@link Exception} → 500 Internal Server Error.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException e) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non authentifié.");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès interdit : droits insuffisants.");
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<String> handleServiceException(ServiceException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    /** Capture toutes les autres exceptions inattendues. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}
