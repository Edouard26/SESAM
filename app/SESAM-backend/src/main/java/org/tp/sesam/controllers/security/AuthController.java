package org.tp.sesam.controllers.security;

import jakarta.servlet.http.HttpServletRequest;
import org.tp.sesam.dtos.AuthDto;
import org.tp.sesam.models.entities.User;
import org.tp.sesam.security.jwt.JwtUtils;
import org.tp.sesam.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Controller d'authentification.
 * <p>
 * POST /api/v1/auth/login    → émet un access token (header) + un refresh token (cookie httpOnly)
 * POST /api/v1/auth/refresh  → rotate le refresh token, émet un nouvel access token
 * POST /api/v1/auth/logout   → révoque le refresh token, efface le cookie
 * <p>
 * Le refresh token est stocké en cookie httpOnly + SameSite=Strict :
 * - Inaccessible depuis JavaScript (protection XSS)
 * - Envoyé uniquement vers /api/v1/auth (path restreint)
 * - Jamais stocké en clair en base (SHA-256)
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.security.refresh-token.expiration-hours:2}")
    private int refreshExpirationHours;

    @Value("${app.security.refresh-token.secure:false}")
    private boolean secureCookie;

    @GetMapping("/csrf")
    public java.util.Map<String, String> csrf(org.springframework.security.web.csrf.CsrfToken token) {
        return java.util.Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * POST /api/v1/auth/login
     * Body   : {"email": "admin@example.fr", "password": "..."}
     * Retour : header Authorization: Bearer {jwt}  +  cookie tp_refresh (httpOnly)
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@jakarta.validation.Valid @RequestBody AuthDto authDto, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDto.getEmail(), authDto.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = (User) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtToken(authentication);
            String rawRefresh = refreshTokenService.createToken(
                    user, extractIp(request), request.getHeader(HttpHeaders.USER_AGENT)
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(rawRefresh).toString())
                    .build();

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * POST /api/v1/auth/refresh
     * Cookie  : tp_refresh (httpOnly, envoyé automatiquement par le navigateur)
     * Retour  : header Authorization: Bearer {new_jwt}  +  nouveau cookie tp_refresh (rotation)
     * <p>
     * En cas de vol détecté (token révoqué réutilisé) → 401 + toute la famille révoquée.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "tp_refresh", required = false) String rawToken,
            HttpServletRequest request) {

        if (rawToken == null) return ResponseEntity.status(401).build();

        try {
            RefreshTokenService.RotateResult result = refreshTokenService.rotate(
                    rawToken, extractIp(request), request.getHeader(HttpHeaders.USER_AGENT)
            );
            String newJwt = jwtUtils.generateJwtTokenForUser(result.user());

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + newJwt)
                    .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.newRawToken()).toString())
                    .build();

        } catch (RefreshTokenService.InvalidRefreshTokenException | RefreshTokenService.RefreshTokenTheftException e) {
            // Token invalide, expiré ou vol détecté → effacer le cookie
            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, buildClearCookie().toString())
                    .build();
        }
    }

    /**
     * POST /api/v1/auth/logout
     * Cookie : tp_refresh — révoque le token en base et efface le cookie.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "tp_refresh", required = false) String rawToken) {

        if (rawToken != null) {
            refreshTokenService.revokeToken(rawToken);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildClearCookie().toString())
                .build();
    }

    /** Retourne uniquement les informations publiques du compte connecté. */
    @GetMapping("/me")
    public org.tp.sesam.dtos.api.MeResponse me(
            @org.springframework.security.core.annotation.AuthenticationPrincipal User user) {
        if (user == null) throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Connexion nécessaire");
        return new org.tp.sesam.dtos.api.MeResponse(user.getId(), user.getNom(), user.getPrenom(),
                user.getEmail(), user.getRole(), user.isActif());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ResponseCookie buildRefreshCookie(String rawToken) {
        return ResponseCookie.from("tp_refresh", rawToken)
                .httpOnly(true)
                .secure(secureCookie)          // configurable pour HTTPS
                .sameSite("Strict")
                .path("/api/v1/auth")   // cookie envoyé uniquement vers les endpoints d'auth
                .maxAge(Duration.ofHours(refreshExpirationHours))
                .build();
    }

    private ResponseCookie buildClearCookie() {
        return ResponseCookie.from("tp_refresh", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
    }

    /**
     * Lit l’adresse vue par le serveur ; configurer les proxies de confiance au niveau serveur.
     */
    private String extractIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
