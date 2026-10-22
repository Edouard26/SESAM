package org.tp.sesam.security.jwt;

import org.tp.sesam.models.entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtils {
    private final SecretKey key;
    private final long expiration;

    public JwtUtils(@Value("${app.security.jwt.secret:}") String secret,
                    @Value("${app.security.jwt.expiration:900000}") long expiration) {
        // Sans configuration, clé aléatoire de développement : les JWT expirent au redémarrage.
        key = secret.isBlank() ? Jwts.SIG.HS256.key().build() : Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        if (expiration <= 0) throw new IllegalArgumentException("Expiration JWT positive obligatoire");
        this.expiration = expiration;
    }

    public String generateJwtToken(Authentication authentication) {
        return generateJwtTokenForUser((User) authentication.getPrincipal());
    }

    public String generateJwtTokenForUser(User user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getUsername()).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiration))).signWith(key, Jwts.SIG.HS256).compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }
}
