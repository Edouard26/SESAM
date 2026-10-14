package net.ent.etnc.backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.ent.etnc.backend.models.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilitaire JWT : génération, validation et parsing des tokens.
 *
 * Algorithme : HS256 (HMAC-SHA256) avec clé secrète.
 * La clé doit faire au moins 64 caractères (512 bits) pour HS512,
 * ou 32 caractères (256 bits) pour HS256.
 *
 * Contenu du token (claims) :
 *   - subject : username
 *   - email   : adresse email de l'utilisateur
 *   - role    : rôle (ADMIN)
 *   - iat     : date d'émission
 *   - exp     : date d'expiration
 */
@Component
public class JwtUtils {

    @Value("${app.security.jwt.secret:default-secret-key-please-change-in-production-32chars}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration:900000}")
    private long jwtExpirationMs;

    /**
     * Génère un access token signé contenant l'email et le rôle de l'utilisateur.
     */
    public String generateJwtToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    /** Extrait l'email (subject) d'un token valide. */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Retourne {@code true} si le token est correctement signé et non expiré.
     * Avale silencieusement toute {@link JwtException} — le filter ignorera simplement ce token.
     */
    public boolean validateJwtToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
