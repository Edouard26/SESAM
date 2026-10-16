package net.ent.etnc.backend.controllers.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.ent.etnc.backend.dtos.requests.LoginRequest;
import net.ent.etnc.backend.dtos.responses.TokenResponse;
import net.ent.etnc.backend.dtos.assemblers.UserAssembler;
import net.ent.etnc.backend.models.entities.User;
import net.ent.etnc.backend.security.jwt.JwtUtils;
import net.ent.etnc.backend.security.services.UserDetailsServiceImpl;
import net.ent.etnc.backend.services.RefreshTokenService;
import net.ent.etnc.backend.services.commons.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * Endpoint d'authentification ({@code /api/v1/auth/}).
 *
 * <p>Stratégie token :
 * <ul>
 *   <li><strong>Access token</strong> — JWT signé retourné dans le corps ({@link TokenResponse}).
 *       Durée configurée par {@code app.jwt.expiration-ms}.</li>
 *   <li><strong>Refresh token</strong> — valeur brute (SHA-256 stocké en base) transmise
 *       via un cookie HttpOnly ({@code certification_center_refresh}), jamais exposée en JSON.</li>
 * </ul>
 *
 * <p>Endpoints :
 * <ul>
 *   <li>{@code POST /login/} — authentification par email/mot de passe.</li>
 *   <li>{@code POST /refresh/} — rotation du refresh token et nouvel access token.</li>
 *   <li>{@code POST /logout/} — suppression du cookie de refresh.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String COOKIE_NAME = "certification_center_refresh";
    private final long refreshExpirationMs;
    private final long jwtExpirationMs;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserAssembler userAssembler;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public AuthController(
            @Value("${app.security.jwt.refresh-expiration-ms}") long refreshExpirationMs,
            @Value("${app.security.jwt.expiration-ms}") long jwtExpirationMs,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            RefreshTokenService refreshTokenService,
            UserAssembler userAssembler,
            UserDetailsServiceImpl userDetailsService) {
        this.refreshExpirationMs = refreshExpirationMs;
        this.jwtExpirationMs = jwtExpirationMs;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.userAssembler = userAssembler;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login/")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = (User) authentication.getPrincipal();
        String accessToken = jwtUtils.generateJwtToken(user);
        String rawRefreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        addRefreshCookie(response, rawRefreshToken);

        return ResponseEntity.ok(new TokenResponse(accessToken, jwtExpirationMs / 1000, userAssembler.toDto(user)));
    }

    @PostMapping("/refresh/")
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = extractRefreshCookie(request);
        if (rawToken == null) {
            throw new ServiceException("RG-SEC10", "Cookie de refresh manquant");
        }

        RefreshTokenService.RotateResult result = refreshTokenService.rotateRefreshToken(rawToken);
        User user = (User) userDetailsService.loadUserByUsername(result.email());
        String newAccessToken = jwtUtils.generateJwtToken(user);

        addRefreshCookie(response, result.rawToken());

        return ResponseEntity.ok(new TokenResponse(newAccessToken, jwtExpirationMs / 1000, userAssembler.toDto(user)));
    }

    @PostMapping("/logout/")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = extractRefreshCookie(request);
        if (rawToken != null) {
            refreshTokenService.revokeToken(rawToken);
        }
        clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    private void addRefreshCookie(HttpServletResponse response, String rawToken) {
        Cookie cookie = new Cookie(COOKIE_NAME, rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true en production HTTPS
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge((int) (refreshExpirationMs / 1000));
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
