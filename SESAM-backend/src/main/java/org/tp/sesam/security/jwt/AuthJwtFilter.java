package org.tp.sesam.security.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthJwtFilter extends OncePerRequestFilter {
    private final JwtUtils jwt;
    private final UserDetailsService users;

    public AuthJwtFilter(JwtUtils jwt, UserDetailsService users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            try {
                var user = users.loadUserByUsername(jwt.getUsernameFromJwtToken(header.substring(7)));
                new AccountStatusUserDetailsChecker().check(user);
                var auth = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | AuthenticationException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                response.sendError(401, "Authentification refusée");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
