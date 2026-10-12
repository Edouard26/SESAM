package org.tp.sesam.services;

import org.tp.sesam.models.entities.User;

/**
 * Service de gestion des refresh tokens avec rotation.
 * <p>
 * Contrat :
 * - createToken()  : émission d'un nouveau token à la connexion
 * - rotate()       : échange d'un token valide contre un nouveau (expiration glissante)
 * - revokeToken()  : révocation unitaire (logout)
 * - revokeAll()    : révocation de toutes les sessions d'un utilisateur
 * <p>
 * Service technique : n'étend pas Service<T,ID> ni AbstractService.
 */
public interface RefreshTokenService {

    /**
     * Crée un nouveau refresh token pour l'utilisateur.
     *
     * @return le token brut (UUID) à placer dans le cookie httpOnly
     */
    String createToken(User user, String ipAddress, String userAgent);

    /**
     * Valide, révoque et remplace le token (rotation).
     * Détection de vol : si le token est déjà révoqué, toute la famille est révoquée.
     *
     * @return le résultat contenant le nouveau token brut et l'utilisateur associé
     * @throws InvalidRefreshTokenException si le token est invalide ou expiré
     * @throws RefreshTokenTheftException   si une réutilisation de token révoqué est détectée
     */
    RotateResult rotate(String rawToken, String ipAddress, String userAgent);

    /**
     * Révoque un token (logout simple). Ne lève pas d'exception si le token est inconnu.
     */
    void revokeToken(String rawToken);

    /**
     * Révoque toutes les sessions actives d'un utilisateur (logout global).
     */
    void revokeAllForUser(User user);

    // ── Types de retour / exceptions ─────────────────────────────────────────

    record RotateResult(String newRawToken, User user) {
    }

    class InvalidRefreshTokenException extends RuntimeException {
        public InvalidRefreshTokenException(String msg) {
            super(msg);
        }
    }

    class RefreshTokenTheftException extends RuntimeException {
        public RefreshTokenTheftException(String msg) {
            super(msg);
        }
    }
}
