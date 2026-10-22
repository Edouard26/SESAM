package org.tp.sesam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tp.sesam.models.entities.RefreshToken;
import org.tp.sesam.models.entities.User;
import org.tp.sesam.repositories.RefreshTokenRepository;
import org.tp.sesam.repositories.UserRepository;
import org.tp.sesam.services.RefreshTokenService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Logger log =
            LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    private final RefreshTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final Duration expiration;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository tokenRepository,
            UserRepository userRepository,
            @Value("${app.security.refresh-token.expiration-hours:2}")
            long expirationHours
    ) {
        if (expirationHours <= 0) {
            throw new IllegalArgumentException(
                    "La durée de validité du refresh token doit être positive."
            );
        }

        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.expiration = Duration.ofHours(expirationHours);
    }

    @Override
    public String createToken(User user, String ipAddress, String userAgent) {
        // Verrou partagé par toutes les opérations de tokens de cet utilisateur.
        User lockedUser = verrouillerUtilisateur(user);

        if (!lockedUser.isEnabled()) {
            throw new InvalidRefreshTokenException("Compte inactif.");
        }

        // Une nouvelle connexion démarre une nouvelle famille de tokens.
        return enregistrerToken(
                lockedUser,
                UUID.randomUUID().toString(),
                ipAddress,
                userAgent
        );
    }

    @Override
    @Transactional(noRollbackFor = {
            InvalidRefreshTokenException.class,
            RefreshTokenTheftException.class
    })
    public RotateResult rotate(
            String rawToken,
            String ipAddress,
            String userAgent
    ) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token absent.");
        }

        String hash = hashToken(rawToken);

        // Recherche seulement l'ID : le token sera chargé APRÈS le verrou.
        Long userId = tokenRepository.findUserIdByTokenHash(hash)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException("Refresh token inconnu.")
                );

        User user = userRepository.findForTokenUpdate(userId)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException("Utilisateur introuvable.")
                );

        RefreshToken current = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException("Refresh token inconnu.")
                );

        // Un token déjà utilisé ne doit jamais permettre une nouvelle rotation.
        if (current.isRevoked()) {
            tokenRepository.revokeAllByFamily(current.getFamily());

            // noRollbackFor conserve cette révocation malgré l'exception.
            throw new RefreshTokenTheftException(
                    "Refresh token réutilisé : session révoquée."
            );
        }

        // Un compte désactivé ne peut pas renouveler sa connexion.
        if (!user.isEnabled()) {
            tokenRepository.revokeAllActiveByUser(user);
            throw new InvalidRefreshTokenException("Compte inactif.");
        }

        if (!current.getExpiresAt().isAfter(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token expiré.");
        }

        // Les informations réseau servent à l'audit, sans bloquer la rotation.
        if (!Objects.equals(current.getIpAddress(), limiter(ipAddress, 45))
                || !Objects.equals(current.getUserAgent(), limiter(userAgent, 512))) {
            log.warn(
                    "Changement IP/User-Agent pendant une rotation pour l'utilisateur {}",
                    user.getId()
            );
        }

        // L'ancien token devient inutilisable.
        // JPA enregistrera ce changement à la fin de la transaction.
        current.setRevoked(true);

        // Le nouveau token conserve la famille et obtient une nouvelle expiration.
        String newRawToken = enregistrerToken(
                user,
                current.getFamily(),
                ipAddress,
                userAgent
        );

        return new RotateResult(newRawToken, user);
    }

    @Override
    public void revokeToken(String rawToken) {
        // Une déconnexion sans token reste une opération valide.
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        String hash = hashToken(rawToken);
        var userId = tokenRepository.findUserIdByTokenHash(hash);

        if (userId.isEmpty()) {
            return;
        }

        // Empêche une rotation concurrente pendant la révocation.
        if (userRepository.findForTokenUpdate(userId.get()).isEmpty()) {
            return;
        }

        tokenRepository.findByTokenHash(hash)
                .ifPresent(token -> token.setRevoked(true));
    }

    @Override
    public void revokeAllForUser(User user) {
        User lockedUser = verrouillerUtilisateur(user);
        tokenRepository.revokeAllActiveByUser(lockedUser);
    }

    /**
     * Enregistre uniquement le hash et retourne le token brut au contrôleur.
     */
    private String enregistrerToken(
            User user,
            String family,
            String ipAddress,
            String userAgent
    ) {
        String rawToken = UUID.randomUUID().toString();
        Instant now = Instant.now();

        RefreshToken token = new RefreshToken();
        token.setTokenHash(hashToken(rawToken));
        token.setUser(user);
        token.setFamily(family);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(expiration));
        token.setRevoked(false);

        // Respecte les longueurs maximales des colonnes d'audit.
        token.setIpAddress(limiter(ipAddress, 45));
        token.setUserAgent(limiter(userAgent, 512));

        tokenRepository.save(token);

        return rawToken;
    }

    /**
     * Sérialise les opérations de tokens d'un même utilisateur en BDD.
     */
    private User verrouillerUtilisateur(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "Un utilisateur enregistré est obligatoire."
            );
        }

        return userRepository.findForTokenUpdate(user.getId())
                .orElseThrow(() ->
                        new InvalidRefreshTokenException("Utilisateur introuvable.")
                );
    }

    /**
     * Convertit le token en hash SHA-256 hexadécimal de 64 caractères.
     */
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible.", e);
        }
    }

    private String limiter(String value, int maximum) {
        if (value == null) {
            return null;
        }

        return value.substring(0, Math.min(value.length(), maximum));
    }
}